package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.osm.StaticMapCreator;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioBasicNetworkPreparer;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;
import ch.ethz.idsc.amodtaxi.trace.ReadTraceFiles;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilterCollection;
import ch.ethz.idsc.amodtaxi.tripfilter.TripNetworkFilter;
import ch.ethz.idsc.tensor.io.DeleteDirectory;
import ch.ethz.idsc.tensor.qty.Quantity;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

public class SanFranciscoScenarioTest {
    private static final int NUM_TAXIS = 2;
    private static final File DIRECTORY = new File(MultiFileTools.getDefaultWorkingDirectory(), "test-scenario");

    @BeforeClass
    public static void setup() throws Exception {
        GlobalAssert.that(DIRECTORY.mkdirs());

        SanFranciscoSetup.in(DIRECTORY); // TODO solve osm file issues: long download, too may retries, too large for github
        StaticMapCreator.now(DIRECTORY);
    }

    @Test
    public void testPipeLine() throws Exception {
        List<File> traceFiles = TraceFileChoice.getDefault().random(NUM_TAXIS);
        Assert.assertTrue(!traceFiles.isEmpty());

        /** prepare the network */
        ScenarioBasicNetworkPreparer.run(DIRECTORY);
        Assert.assertTrue(new File(DIRECTORY, ScenarioLabels.network).exists());
        Assert.assertTrue(new File(DIRECTORY, ScenarioLabels.networkGz).exists());

        /** based on the taxi data, create a population and assemble a AMoDeus scenario */
        ScenarioOptions scenarioOptions = new ScenarioOptions(DIRECTORY, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        Assert.assertTrue(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        final Network network = NetworkLoader.fromNetworkFile(new File(DIRECTORY, configFull.network().getInputFile()));
        Assert.assertTrue(!network.getLinks().isEmpty());

        /** get dayTaxiRecord from trace files */
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());
        FastLinkLookup fll = new FastLinkLookup(network, db);
        CsvFleetReaderSF reader = new CsvFleetReaderSF(new DayTaxiRecordSF(db, fll));
        DayTaxiRecord dayTaxiRecord = ReadTraceFiles.in(traceFiles, reader);
        Assert.assertEquals(NUM_TAXIS, dayTaxiRecord.numTaxis());

        TaxiTripFilterCollection tripFilter = new TaxiTripFilterCollection();
        /** trips which are faster than the network freeflow speeds would allow are removed */
        tripFilter.addFilter(new TripNetworkFilter(network, db, //
                Quantity.of(2.235200008, "m*s^-1"), Quantity.of(3600, "s"), Quantity.of(200, "m"), true));
        StandaloneFleetConverterSF sfc = new StandaloneFleetConverterSF(DIRECTORY, dayTaxiRecord, db, network, //
                Quantity.of(10, SI.SECOND), new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles")), //
                tripFilter, tripFilter);
        sfc.run(LocalDate.of(2008, 6, 4));
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        DeleteDirectory.of(DIRECTORY, 6, 10_000);
    }
}
