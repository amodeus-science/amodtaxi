/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import ch.ethz.idsc.amodtaxi.osm.StaticMapCreator;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.taxitrip.ImportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.fleetconvert.ChicagoOnlineTripFleetConverter;
import ch.ethz.idsc.amodtaxi.fleetconvert.ReaderTripFleetConverter;
import ch.ethz.idsc.amodtaxi.linkspeed.iterative.IterativeLinkSpeedEstimator;
import ch.ethz.idsc.amodtaxi.scenario.FinishedScenario;
import ch.ethz.idsc.amodtaxi.scenario.Scenario;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioBasicNetworkPreparer;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsReader;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilterCollection;
import ch.ethz.idsc.amodtaxi.tripfilter.TripNetworkFilter;
import ch.ethz.idsc.amodtaxi.tripmodif.ChicagoFormatModifier;
import ch.ethz.idsc.amodtaxi.tripmodif.OriginDestinationCentroidResampling;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifier;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifierCollection;
import ch.ethz.idsc.amodtaxi.tripmodif.TripStartTimeShiftResampling;
import ch.ethz.idsc.tensor.io.DeleteDirectory;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class CreateChicagoScenario {
    private static final AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Chicago"));
    private static final Random RANDOM = new Random(123);

    private static void createScenario(File workingDir) throws Exception {
        ChicagoSetup.in(workingDir);

        // FIXME remove debug loop once done
        boolean debug = false;

        /** download of open street map data to create scenario */
        StaticMapCreator.now(workingDir);

        /** prepare the network */
        ScenarioBasicNetworkPreparer.run(workingDir);

        /** load taxi data for the city of Chicago */
        File tripFile;
        if (!debug) {
            tripFile = ChicagoDataLoader.from(ScenarioLabels.amodeusFile, workingDir);
        } else {
            tripFile = new File(workingDir, "/Taxi_Trips_2019_07_19.csv");
        }

        File processingDir = new File(workingDir, "Scenario");
        if (processingDir.isDirectory())
            DeleteDirectory.of(processingDir, 2, 25);
        if (!processingDir.isDirectory())
            processingDir.mkdir();

        CopyFiles.now(workingDir.getAbsolutePath(), processingDir.getAbsolutePath(), //
                Arrays.asList(ScenarioLabels.amodeusFile, ScenarioLabels.config, ScenarioLabels.network, ScenarioLabels.networkGz, ScenarioLabels.LPFile));
        ScenarioOptions scenarioOptions = new ScenarioOptions(processingDir, //
                ScenarioOptionsBase.getDefault());
        LocalDate simulationDate = LocalDateConvert.ofOptions(scenarioOptions.getString("date"));

        /** based on the taxi data, create a population and assemble a AMoDeus scenario */
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        System.out.println(configFile.getAbsolutePath());
        GlobalAssert.that(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        final Network network = NetworkLoader.fromNetworkFile(new File(processingDir, configFull.network().getInputFile()));
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());
        FastLinkLookup fll = new FastLinkLookup(network, db);

        /** prepare for creation of scenario */
        TaxiTripsReader tripsReader = new OnlineTripsReaderChicago();
        TaxiDataModifier tripModifier;
        // = ChicagoOnlineTripBasedModifier.create(random, network, //
        // fll, new File(processingdir, "virtualNetworkChicago"));
        {
            TaxiDataModifierCollection taxiDataModifierCollection = new TaxiDataModifierCollection();
            /** below filter was removed as it causes request spikes at quarter hour intervals,
             * see class for detailed description */
            // addModifier(new ChicagoTripStartTimeResampling(random));
            /** instead the TripStartTimeShiftResampling is used: */
            taxiDataModifierCollection.addModifier(new TripStartTimeShiftResampling(RANDOM, Quantity.of(900, SI.SECOND)));
            /** TODO RVM document why centroid resampling */
            File vNFile = new File(processingDir, "virtualNetworkChicago");
            taxiDataModifierCollection.addModifier(new OriginDestinationCentroidResampling(RANDOM, network, fll, vNFile));
            tripModifier = taxiDataModifierCollection;
        }
        TaxiTripFilterCollection taxiTripFilterCollection = new TaxiTripFilterCollection();
        /** trips which are faster than the network freeflow speeds would allow are removed */
        taxiTripFilterCollection.addFilter(new TripNetworkFilter(network, db, //
                Quantity.of(2.235200008, "m*s^-1"), Quantity.of(3600, "s"), Quantity.of(200, "m"), true));

        // TODO eventually remove, this did not improve the fit.
        // finalFilters.addFilter(new TripMaxSpeedFilter(network, db, ScenarioConstants.maxAllowedSpeed));
        File destinDir = new File(workingDir, "CreatedScenario");
        List<TaxiTrip> finalTrips;
        { // prepare final scenario
            ReaderTripFleetConverter converter = //
                    new ChicagoOnlineTripFleetConverter(scenarioOptions, network, tripModifier, //
                            new ChicagoFormatModifier(), taxiTripFilterCollection, tripsReader, tripFile, new File(processingDir, "tripData"));
            File finalTripsFile = Scenario.create(workingDir, tripFile, converter, processingDir, simulationDate, timeConvert);

            Objects.requireNonNull(finalTripsFile);

            System.out.println("The final trips file is: ");
            System.out.println(finalTripsFile.getAbsolutePath());

            // this is the old LP-based code
            // ChicagoLinkSpeeds.compute(processingDir, finalTripsFile);
            // new code

            /** loading final trips */
            finalTrips = ImportTaxiTrips.fromFile(finalTripsFile);
        }
        final int maxIter = 100000;
        new IterativeLinkSpeedEstimator(maxIter).compute(processingDir, network, db, finalTrips);

        FinishedScenario.copyToDir(processingDir.getAbsolutePath(), //
                destinDir.getAbsolutePath(), //
                new String[] { //
                        ScenarioLabels.amodeusFile, ScenarioLabels.networkGz, ScenarioLabels.populationGz, //
                        ScenarioLabels.LPFile, ScenarioLabels.config, "virtualNetworkChicago", ScenarioLabels.linkSpeedData });
        cleanUp(workingDir);
    }

    static private void cleanUp(File workingDir) {
        /** delete unneeded files */
        // DeleteDirectory.of(new File(workingDir, "Scenario"), 2, 14);
        // DeleteDirectory.of(new File(workingDir, ScenarioLabels.amodeusFile), 0, 1);
        // DeleteDirectory.of(new File(workingDir, ScenarioLabels.avFile), 0, 1);
        // DeleteDirectory.of(new File(workingDir, ScenarioLabels.config), 0, 1);
        // DeleteDirectory.of(new File(workingDir, ScenarioLabels.pt2MatSettings), 0, 1);
        // DeleteDirectory.of(new File(workingDir, ScenarioLabels.network), 0, 1);
    }

    /** in @param args[0] working directory (empty directory), this main function will create
     * an AMoDeus scenario based on the Chicago taxi dataset available online.
     * Settings can afterwards be changed in the AmodeusOptions.properties file located
     * in the directory.
     * 
     * @throws Exception */
    public static void main(String[] args) throws Exception {
        File workingDir = new File(args[0]);
        createScenario(workingDir);
    }
}
