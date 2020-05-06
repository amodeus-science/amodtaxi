package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.fleetconvert.TripFleetConverter;
import ch.ethz.idsc.amodtaxi.scenario.FinishedScenario;
import ch.ethz.idsc.amodtaxi.scenario.Scenario;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;
import ch.ethz.idsc.amodtaxi.trace.ReadTraceFiles;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilterCollection;
import ch.ethz.idsc.amodtaxi.tripfilter.TripNetworkFilter;
import ch.ethz.idsc.amodtaxi.tripmodif.NullModifier;
import ch.ethz.idsc.tensor.io.DeleteDirectory;
import ch.ethz.idsc.tensor.qty.Quantity;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;

public class SanFranciscoScenarioTest {
    private static final int NUM_TAXIS = 2;
    private static final File DIRECTORY = new File(MultiFileTools.getDefaultWorkingDirectory(), "test-scenario");

    @BeforeClass
    public static void setup() throws Exception {
        GlobalAssert.that(DIRECTORY.mkdirs());
        SanFranciscoTestSetup.in(DIRECTORY);
    }

    @Test
    public void testPipeLineNew() throws Exception {
        List<File> traceFiles = TraceFileChoice.getDefault().random(NUM_TAXIS);
        Assert.assertTrue(!traceFiles.isEmpty());

        /** prepare the network */
        File processingDir = new File(DIRECTORY, "Scenario");
        processingDir.mkdir();

        CopyFiles.now(DIRECTORY.getAbsolutePath(), processingDir.getAbsolutePath(), //
                Arrays.asList(ScenarioLabels.amodeusFile, ScenarioLabels.config, ScenarioLabels.network, ScenarioLabels.networkGz, ScenarioLabels.LPFile));
        Assert.assertTrue(new File(processingDir, ScenarioLabels.network).exists());
        Assert.assertTrue(new File(processingDir, ScenarioLabels.networkGz).exists());

        /** based on the taxi data, create a population and assemble a AMoDeus scenario */
        ScenarioOptions scenarioOptions = new ScenarioOptions(processingDir, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        Assert.assertTrue(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        final Network network = NetworkLoader.fromNetworkFile(new File(processingDir, configFull.network().getInputFile()));
        Assert.assertTrue(!network.getLinks().isEmpty()); // 16'882

        /** get dayTaxiRecord from trace files */
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());
        FastLinkLookup fll = new FastLinkLookup(network, db);
        CsvFleetReaderSF reader = new CsvFleetReaderSF(new DayTaxiRecordSF(fll));
        DayTaxiRecord dayTaxiRecord = ReadTraceFiles.in(traceFiles, reader);
        Assert.assertEquals(NUM_TAXIS, dayTaxiRecord.numTaxis());

        LocalDate simulationDate = LocalDate.of(2008, 6, 4);
        TaxiTripFilterCollection tripFilter = new TaxiTripFilterCollection();
        /** trips which are faster than the network freeflow speeds would allow are removed */
        tripFilter.addFilter(new TripNetworkFilter(network, db, //
                Quantity.of(2.235200008, SI.VELOCITY), Quantity.of(3600, SI.SECOND), Quantity.of(200, SI.METER), true));

        File outputDirectory = new File(DIRECTORY, simulationDate.toString());
        TripFleetConverter converter = new SanFranciscoTripFleetConverter( //
                scenarioOptions, network, dayTaxiRecord, simulationDate, NullModifier.INSTANCE, tripFilter, outputDirectory);
        File finalTripsFile = Scenario.create(DIRECTORY, converter, processingDir, simulationDate, new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles")));

        // List<TaxiTrip> finalTrips = ImportTaxiTrips.fromFile(finalTripsFile);
        // final int maxIter = 100;
        // new IterativeLinkSpeedEstimator(maxIter).compute(processingDir, network, db, finalTrips);

        FinishedScenario.copyToDir(processingDir.getAbsolutePath(), outputDirectory.getAbsolutePath(), //
                new String[] { //
                        ScenarioLabels.amodeusFile, ScenarioLabels.networkGz, ScenarioLabels.populationGz, //
                        ScenarioLabels.LPFile, ScenarioLabels.config, ScenarioLabels.linkSpeedData });

        Config createdConfig = ConfigUtils.loadConfig(new File(outputDirectory, ScenarioLabels.config).toString());
        Network createdNetwork = ScenarioUtils.loadScenario(createdConfig).getNetwork();
        Population createdPopulation = ScenarioUtils.loadScenario(createdConfig).getPopulation();

        Assert.assertTrue(!createdNetwork.getLinks().isEmpty()); // 16'882
        Assert.assertTrue(!createdPopulation.getPersons().isEmpty()); // 25
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        DeleteDirectory.of(DIRECTORY, 6, 10_000);
    }
}
