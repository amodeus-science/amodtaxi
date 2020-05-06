/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;

import com.google.common.io.Files;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.taxitrip.ImportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.fleetconvert.ChicagoOnlineTripFleetConverter;
import ch.ethz.idsc.amodtaxi.fleetconvert.TripFleetConverter;
import ch.ethz.idsc.amodtaxi.linkspeed.iterative.IterativeLinkSpeedEstimator;
import ch.ethz.idsc.amodtaxi.scenario.FinishedScenario;
import ch.ethz.idsc.amodtaxi.scenario.Pt2MatsimXML;
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

/** Tests if data for the creation of the Chicago taxi scenario is accessible from the web API. */
public class CreateChicagoScenarioTest {
    private static final AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Chicago"));
    private static final Random RANDOM = new Random(123);

    @Test
    public void test() throws Exception {
        /* Init */
        File workingDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "test");
        File osmFile = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/testScenario/mapChicago.osm");
        Assert.assertTrue(workingDir.exists() || workingDir.mkdir());
        ChicagoSetup.in(workingDir);

        /* Reduce population size in Properties */
        String smallProp = ScenarioLabels.amodeusFile;
        File smallPropFile = new File(workingDir, smallProp);
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(new File(workingDir, ScenarioLabels.amodeusFile))) {
            properties.load(fis);
        }
        properties.setProperty(ScenarioOptionsBase.MAXPOPULATIONSIZEIDENTIFIER, "100");
        FileOutputStream out = new FileOutputStream(smallPropFile);
        properties.store(out, null);
        out.close();

        /* Download of open street map data to create scenario */
        Files.copy(osmFile, new File(workingDir, ScenarioLabels.osmData)); // from boundingBox={-87.7034, 41.8997, -87.6989, 41.9041}
        Osm2MultimodalNetwork.run(new File(workingDir, ScenarioLabels.pt2MatSettings).getAbsolutePath());

        /* Prepare the network */
        ScenarioBasicNetworkPreparer.run(workingDir);

        /* Load taxi data for the city of Chicago */
        File tripFile = ChicagoDataLoader.from(ScenarioLabels.amodeusFile, workingDir);

        /* Create empty scenario folder */
        File processingDir = new File(workingDir, "Scenario");
        if (processingDir.isDirectory())
            DeleteDirectory.of(processingDir, 3, 100);
        if (!processingDir.isDirectory())
            processingDir.mkdir();

        CopyFiles.now(workingDir.getAbsolutePath(), processingDir.getAbsolutePath(), Arrays.asList(//
                ScenarioLabels.amodeusFile, //
                ScenarioLabels.config, //
                ScenarioLabels.network, //
                ScenarioLabels.networkGz, //
                ScenarioLabels.LPFile));
        ScenarioOptions scenarioOptions = new ScenarioOptions(processingDir, //
                ScenarioOptionsBase.getDefault());
        LocalDate simulationDate = LocalDateConvert.ofOptions(scenarioOptions.getString("date"));

        /* Based on the taxi data, create a population and assemble a AMoDeus scenario */
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        GlobalAssert.that(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        final Network network = NetworkLoader.fromNetworkFile(new File(processingDir, configFull.network().getInputFile()));
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());
        FastLinkLookup fll = new FastLinkLookup(network, db);

        /* Prepare for creation of scenario */
        TaxiTripsReader tripsReader = new OnlineTripsReaderChicago();
        TaxiDataModifier tripModifier;

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

        TaxiTripFilterCollection taxiTripFilterCollection = new TaxiTripFilterCollection();
        /** trips which are faster than the network freeflow speeds would allow are removed */
        taxiTripFilterCollection.addFilter(new TripNetworkFilter(network, db, //
                Quantity.of(2.235200008, SI.VELOCITY), Quantity.of(3600, SI.SECOND), Quantity.of(200, SI.METER), true));

        // TODO eventually remove, this did not improve the fit.
        // finalFilters.addFilter(new TripMaxSpeedFilter(network, db, ScenarioConstants.maxAllowedSpeed));
        File destinDir = new File(workingDir, "CreatedScenario");
        List<TaxiTrip> finalTrips;

        // prepare final scenario
        TripFleetConverter converter = //
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

        final int maxIter = 1000;
        new IterativeLinkSpeedEstimator(maxIter, RANDOM).compute(processingDir, network, db, finalTrips);

        FinishedScenario.copyToDir(processingDir.getAbsolutePath(), //
                destinDir.getAbsolutePath(), //
                new String[] { //
                        ScenarioLabels.amodeusFile, ScenarioLabels.networkGz, ScenarioLabels.populationGz, //
                        ScenarioLabels.LPFile, ScenarioLabels.config, "virtualNetworkChicago", ScenarioLabels.linkSpeedData });

        /* Clean up */
        Assert.assertTrue(workingDir.exists());
        DeleteDirectory.of(workingDir, 3, 100);
        Assert.assertFalse(workingDir.exists());
    }
}
