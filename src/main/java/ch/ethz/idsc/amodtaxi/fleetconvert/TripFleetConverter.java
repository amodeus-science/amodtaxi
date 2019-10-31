/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.fleetconvert;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.taxitrip.ExportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.CreateQuadTree;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.population.TripPopulationCreator;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsReader;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilter;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifier;
import ch.ethz.idsc.amodtaxi.tripmodif.TripBasedModifier;

public abstract class TripFleetConverter {

    protected final ScenarioOptions scenarioOptions;
    protected final Network network;
    protected final TaxiTripFilter primaryFilter = new TaxiTripFilter();
    protected final TripBasedModifier contentModifier;
    protected final TaxiDataModifier formatModifier;
    protected final TaxiTripFilter finalFilters;
    protected final TaxiTripsReader tripsReader;
    protected final MatsimAmodeusDatabase db;
    protected final QuadTree<Link> qt;

    private File finalTripsFile = null;

    public TripFleetConverter(ScenarioOptions scenarioOptions, Network network, //
            TripBasedModifier tripModifier, //
            TaxiDataModifier generalModifier, TaxiTripFilter finalFilters, //
            TaxiTripsReader tripsReader) {
        this.scenarioOptions = scenarioOptions;
        this.network = network;
        this.contentModifier = tripModifier;
        this.formatModifier = generalModifier;
        this.finalFilters = finalFilters;
        this.tripsReader = tripsReader;
        ReferenceFrame referenceFrame = scenarioOptions.getLocationSpec().referenceFrame();
        this.db = MatsimAmodeusDatabase.initialize(network, referenceFrame);
        this.qt = CreateQuadTree.of(network);
    }

    public void run(File processingDir, File tripFile, LocalDate simulationDate, AmodeusTimeConvert timeConvert)//
            throws Exception {
        GlobalAssert.that(tripFile.isFile());

        /** preparation of necessary data */
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        GlobalAssert.that(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());

        /** folder for processing stored files, the folder tripData contains
         * .csv versions of all processing steps for faster debugging. */
        File tripDataDir = new File(processingDir, "tripData");
        tripDataDir.mkdirs();
        FileUtils.copyFileToDirectory(tripFile, tripDataDir);
        File newTripFile = new File(tripDataDir, tripFile.getName());
        System.out.println("NewTripFile: " + newTripFile.getAbsolutePath());
        GlobalAssert.that(newTripFile.isFile());

        /** initial formal modifications, e.g., replacing certain characters,
         * other modifications should be done in the third step */
        File preparedFile = formatModifier.modify(newTripFile);
        Stream<TaxiTrip> stream = tripsReader.getTripStream(preparedFile);
        List<TaxiTrip> allTrips = stream.collect(Collectors.toList());
        System.out.println("Before primary filter: " + allTrips.size());

        /** filtering of trips, e.g., removal of 0 [s] trips */
        Stream<TaxiTrip> filteredStream = primaryFilter.filterStream(allTrips.stream());
        List<TaxiTrip> primaryFiltered = filteredStream.collect(Collectors.toList());
        System.out.println("Primary filtered: " + primaryFiltered.size());
        String filteredFileName = FilenameUtils.getBaseName(preparedFile.getPath()) + "_filtered." + //
                FilenameUtils.getExtension(preparedFile.getPath());
        primaryFilter.printSummary();

        File filteredFile = new File(preparedFile.getParentFile(), filteredFileName);
        ExportTaxiTrips.toFile(primaryFiltered.stream(), filteredFile);
        GlobalAssert.that(filteredFile.isFile());

        /** save unreadable trips for post-processing, checking */
        File unreadable = new File(preparedFile.getParentFile(), //
                FilenameUtils.getBaseName(preparedFile.getAbsolutePath()) + "_unreadable." + //
                        FilenameUtils.getExtension(preparedFile.getAbsolutePath()));
        tripsReader.saveUnreadable(unreadable);

        /** modifying the trip data, e.g., distributing in 15 minute steps. */
        File modifiedTripsFile = contentModifier.modify(filteredFile);
        GlobalAssert.that(modifiedTripsFile.isFile());

        /** creating population based on corrected, filtered file */
        TripPopulationCreator populationCreator = //
                new TripPopulationCreator(processingDir, configFull, network, db, //
                        qt, simulationDate, timeConvert, finalFilters);
        populationCreator.process(modifiedTripsFile);
        finalTripsFile = populationCreator.getFinalTripFile();

        System.exit(1);
    }

    public File getFinalTripFile() {
        return finalTripsFile;
    }

    public abstract void setFilters();
}
