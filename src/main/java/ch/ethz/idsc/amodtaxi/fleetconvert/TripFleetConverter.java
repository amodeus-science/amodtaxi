/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.fleetconvert;

import java.io.File;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.taxitrip.ExportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.CreateQuadTree;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.population.TripPopulationCreator;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsSupplier;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilterCollection;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifier;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.utils.collections.QuadTree;

public abstract class TripFleetConverter {
    private final TaxiDataModifier contentModifier;
    private final TaxiTripFilterCollection finalFilters;
    private final TaxiTripsSupplier taxiTripsSupplier;
    private File finalTripsFile = null;
    protected final Config config;
    protected final Network network;
    protected final MatsimAmodeusDatabase db;
    protected final QuadTree<Link> qt; // TODO replace by FastLinkLookup
    protected final File targetDirectory;
    protected final TaxiTripFilterCollection primaryFilter = new TaxiTripFilterCollection();

    public TripFleetConverter(ScenarioOptions scenarioOptions, Network network, //
            TaxiDataModifier tripModifier, TaxiTripFilterCollection finalFilters, //
            TaxiTripsSupplier taxiTripsSupplier, File targetDirectory) {
        this.network = network;
        this.contentModifier = tripModifier;
        this.finalFilters = finalFilters;
        this.taxiTripsSupplier = taxiTripsSupplier;
        this.targetDirectory = targetDirectory;
        this.targetDirectory.mkdirs();
        ReferenceFrame referenceFrame = scenarioOptions.getLocationSpec().referenceFrame();
        this.db = MatsimAmodeusDatabase.initialize(network, referenceFrame);
        this.qt = CreateQuadTree.of(network);

        File configFile = new File(scenarioOptions.getPreparerConfigName());
        GlobalAssert.that(configFile.exists());
        config = ConfigUtils.loadConfig(configFile.toString());
    }

    public void run(File processingDir, String baseName, String extension, LocalDate simulationDate, AmodeusTimeConvert timeConvert) throws Exception {
        extension = extension.startsWith(".") ? extension : ("." + extension);

        Collection<TaxiTrip> allTrips = taxiTripsSupplier.get();
        System.out.println("Before primary filter: " + allTrips.size());

        /** filtering of trips, e.g., removal of 0 [s] trips */
        Stream<TaxiTrip> filteredStream = primaryFilter.filterStream(allTrips.stream());
        List<TaxiTrip> primaryFiltered = filteredStream.collect(Collectors.toList());
        System.out.println("Primary filtered: " + primaryFiltered.size());
        String filteredFileName = baseName + "_filtered" + extension;
        primaryFilter.printSummary();

        File filteredFile = new File(targetDirectory, filteredFileName);
        ExportTaxiTrips.toFile(primaryFiltered.stream(), filteredFile);
        GlobalAssert.that(filteredFile.isFile());

        /** modifying the trip data, e.g., distributing in 15 minute steps. */
        File modifiedTripsFile = contentModifier.modify(filteredFile);
        GlobalAssert.that(modifiedTripsFile.isFile());

        /** creating population based on corrected, filtered file */
        TripPopulationCreator populationCreator = //
                new TripPopulationCreator(processingDir, config, network, db, //
                        qt, simulationDate, timeConvert, finalFilters);
        populationCreator.process(modifiedTripsFile);
        finalTripsFile = populationCreator.getFinalTripFile();
    }

    public Optional<File> getFinalTripFile() {
        return Optional.ofNullable(finalTripsFile);
    }

    public abstract void setFilters();
}
