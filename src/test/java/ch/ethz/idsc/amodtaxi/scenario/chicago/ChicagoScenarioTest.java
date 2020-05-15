/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import ch.ethz.idsc.amodtaxi.scenario.TestDirectories;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

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
import ch.ethz.idsc.amodtaxi.fleetconvert.TripFleetConverter;
import ch.ethz.idsc.amodtaxi.linkspeed.iterative.IterativeLinkSpeedEstimator;
import ch.ethz.idsc.amodtaxi.scenario.FinishedScenario;
import ch.ethz.idsc.amodtaxi.scenario.Scenario;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsReader;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilterCollection;
import ch.ethz.idsc.amodtaxi.tripfilter.TripNetworkFilter;
import ch.ethz.idsc.amodtaxi.tripmodif.ChicagoFormatModifier;
import ch.ethz.idsc.amodtaxi.tripmodif.OriginDestinationCentroidResampling;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifierCollection;
import ch.ethz.idsc.amodtaxi.tripmodif.TripStartTimeShiftResampling;
import ch.ethz.idsc.tensor.io.DeleteDirectory;
import ch.ethz.idsc.tensor.qty.Quantity;

/** Tests if data for the creation of the Chicago taxi scenario is accessible from the web API. */
public class ChicagoScenarioTest {
    private static final Random RANDOM = new Random(123);
    private static final AmodeusTimeConvert TIME_CONVERT = new AmodeusTimeConvert(ZoneId.of("America/Chicago"));

    @BeforeClass
    public static void setUp() throws Exception {
        GlobalAssert.that(TestDirectories.WORKING.mkdirs());
        ChicagoTestSetup.in(TestDirectories.WORKING);
    }

    @Test
    public void creationTest() throws Exception {
        /* Load taxi data for the city of Chicago */
        File tripFile = ChicagoDataLoader.from(ScenarioLabels.amodeusFile, TestDirectories.WORKING);

        /* Create empty scenario folder */
        File processingDir = new File(TestDirectories.WORKING, "Scenario");
        processingDir.mkdir();

        CopyFiles.now(TestDirectories.WORKING.getAbsolutePath(), processingDir.getAbsolutePath(), Arrays.asList(//
                ScenarioLabels.amodeusFile, ScenarioLabels.config, ScenarioLabels.network, ScenarioLabels.networkGz, ScenarioLabels.LPFile));
        Assert.assertTrue(new File(processingDir, ScenarioLabels.network).exists());
        Assert.assertTrue(new File(processingDir, ScenarioLabels.networkGz).exists());

        /* Based on the taxi data, create a population and assemble a AMoDeus scenario */
        ScenarioOptions scenarioOptions = new ScenarioOptions(processingDir, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        Assert.assertTrue(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        final Network network = NetworkLoader.fromNetworkFile(new File(processingDir, configFull.network().getInputFile()));
        Assert.assertFalse(network.getLinks().isEmpty());
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());
        FastLinkLookup fll = new FastLinkLookup(network, db);

        /* Prepare for creation of scenario */
        TaxiTripsReader tripsReader = new OnlineTripsReaderChicago();

        TaxiDataModifierCollection taxiDataModifierCollection = new TaxiDataModifierCollection();
        /** below filter was removed as it causes request spikes at quarter hour intervals,
         * see class for detailed description */
        // addModifier(new ChicagoTripStartTimeResampling(random));
        /** instead the TripStartTimeShiftResampling is used: */
        taxiDataModifierCollection.addModifier(new TripStartTimeShiftResampling(RANDOM, Quantity.of(900, SI.SECOND)));
        /** TODO RVM document why centroid resampling */
        File vNFile = new File(processingDir, "virtualNetworkChicago");
        taxiDataModifierCollection.addModifier(new OriginDestinationCentroidResampling(RANDOM, network, fll, vNFile));

        LocalDate simulationDate = LocalDateConvert.ofOptions(scenarioOptions.getString("date"));
        TaxiTripFilterCollection taxiTripFilterCollection = new TaxiTripFilterCollection();
        /** trips which are faster than the network freeflow speeds would allow are removed */
        taxiTripFilterCollection.addFilter(new TripNetworkFilter(network, db, //
                Quantity.of(2.235200008, SI.VELOCITY), Quantity.of(3600, SI.SECOND), Quantity.of(200, SI.METER), true));
        // TODO eventually remove, this did not improve the fit.
        // taxiTripFilterCollection.addFilter(new TripMaxSpeedFilter(network, db, ScenarioConstants.maxAllowedSpeed));

        /** prepare final scenario */
        File destinDir = new File(TestDirectories.WORKING, "CreatedScenario");
        TripFleetConverter converter = //
                new ChicagoOnlineTripFleetConverter(scenarioOptions, network, taxiDataModifierCollection, //
                        new ChicagoFormatModifier(), taxiTripFilterCollection, tripsReader, tripFile, new File(processingDir, "tripData"));
        File finalTripsFile = Scenario.create(TestDirectories.WORKING, tripFile, converter, processingDir, simulationDate, TIME_CONVERT);

        Objects.requireNonNull(finalTripsFile);

        System.out.println("The final trips file is: ");
        System.out.println(finalTripsFile.getAbsolutePath());

        /** loading final trips */
        List<TaxiTrip> finalTrips = ImportTaxiTrips.fromFile(finalTripsFile);
        final int maxIter = 1000;
        new IterativeLinkSpeedEstimator(maxIter, RANDOM).compute(processingDir, network, db, finalTrips);

        FinishedScenario.copyToDir(processingDir.getAbsolutePath(), //
                destinDir.getAbsolutePath(), //
                ScenarioLabels.amodeusFile, ScenarioLabels.networkGz, ScenarioLabels.populationGz, //
                ScenarioLabels.LPFile, ScenarioLabels.config, "virtualNetworkChicago", ScenarioLabels.linkSpeedData);

        Config createdConfig = ConfigUtils.loadConfig(new File(destinDir, ScenarioLabels.config).toString());
        Network createdNetwork = ScenarioUtils.loadScenario(createdConfig).getNetwork();
        Population createdPopulation = ScenarioUtils.loadScenario(createdConfig).getPopulation();

        Assert.assertFalse(createdNetwork.getLinks().isEmpty());
        Assert.assertFalse(createdPopulation.getPersons().isEmpty());
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        DeleteDirectory.of(TestDirectories.WORKING, 3, 100);
    }
}
