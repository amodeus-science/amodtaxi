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
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.taxitrip.ExportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.population.TripPopulationCreator;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsSupplier;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilterCollection;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifier;
import ch.ethz.idsc.amodtaxi.util.NamingConvention;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

public abstract class TripFleetConverter {
    private final TaxiDataModifier contentModifier;
    private final TaxiTripFilterCollection finalFilters;
    private final TaxiTripsSupplier taxiTripsSupplier;
    private File finalTripsFile = null;
    protected final Config config;
    protected final Network network;
    protected final MatsimAmodeusDatabase db;
    protected final FastLinkLookup fastLinkLookup;
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
        ReferenceFrame referenceFrame = scenarioOptions.getLocationSpec().referenceFrame();
        db = MatsimAmodeusDatabase.initialize(network, referenceFrame);
        fastLinkLookup = new FastLinkLookup(network, db);

        File configFile = new File(scenarioOptions.getPreparerConfigName());
        GlobalAssert.that(configFile.exists());
        config = ConfigUtils.loadConfig(configFile.toString());
    }

    public void run(File processingDir, NamingConvention convention, LocalDate simulationDate, AmodeusTimeConvert timeConvert) throws Exception {
        targetDirectory.mkdirs();

        Collection<TaxiTrip> allTrips = taxiTripsSupplier.get();
        System.out.println("Before primary filter: " + allTrips.size());

        /** filtering of trips, e.g., removal of 0 [s] trips */
        Stream<TaxiTrip> filteredStream = primaryFilter.filterStream(allTrips.stream());
        List<TaxiTrip> primaryFiltered = filteredStream.collect(Collectors.toList());
        System.out.println("Primary filtered: " + primaryFiltered.size());
        String filteredFileName = convention.apply("filtered");
        primaryFilter.printSummary();

        File filteredFile = new File(targetDirectory, filteredFileName);
        ExportTaxiTrips.toFile(primaryFiltered.stream(), filteredFile); // parent directory must exist beforehand
        GlobalAssert.that(filteredFile.isFile());

        /** modifying the trip data, e.g., distributing in 15 minute steps. */
        File modifiedTripsFile = contentModifier.modify(filteredFile);
        GlobalAssert.that(modifiedTripsFile.isFile());

        /** creating population based on corrected, filtered file */
        TripPopulationCreator populationCreator = new TripPopulationCreator(processingDir, config, network, fastLinkLookup, simulationDate, timeConvert, finalFilters);
        populationCreator.process(modifiedTripsFile);
        finalTripsFile = populationCreator.getFinalTripFile();
    }

    public Optional<File> getFinalTripFile() {
        return Optional.ofNullable(finalTripsFile);
    }

    public abstract void setFilters();
}
