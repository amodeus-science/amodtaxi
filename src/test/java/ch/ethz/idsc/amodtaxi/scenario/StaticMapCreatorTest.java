/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.osm.StaticMapCreator;
import ch.ethz.idsc.tensor.io.DeleteDirectory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;

public class StaticMapCreatorTest {
    private static final File WORKING_DIR = new File(Locate.repoFolder(ScenarioBasicNetworkPreparer.class, "amodtaxi"), "test-scenario");
    private static final File RESOURCE_DIR = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/test/miniScenario");

    @BeforeClass
    public static void setup() throws Exception {
        WORKING_DIR.mkdir();

        CopyFiles.now(RESOURCE_DIR.getAbsolutePath(), WORKING_DIR.getAbsolutePath(), //
                Arrays.asList(ScenarioLabels.osmData, ScenarioLabels.amodeusFile, ScenarioLabels.pt2MatSettings), true);
        /** change pt2Matsim settings to local file system */
        Pt2MatsimXML.toLocalFileSystem(new File(WORKING_DIR, ScenarioLabels.pt2MatSettings), WORKING_DIR.getAbsolutePath());
    }

    @Test
    public void test() throws IOException {
        /* Run function of interest */
        StaticMapCreator.now(WORKING_DIR);

        /* Check functionality */
        Assert.assertTrue(new File(WORKING_DIR, "networkPt2Matsim.xml.gz").exists());

        Network network = NetworkLoader.fromNetworkFile(new File(WORKING_DIR, "networkPt2Matsim.xml.gz"));
        Network referenceNetwork = NetworkLoader.fromNetworkFile(new File(WORKING_DIR, "networkPt2Matsim.xml.gz"));
        Assert.assertEquals(referenceNetwork.getLinks().size(), network.getLinks().size());
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        DeleteDirectory.of(WORKING_DIR, 2, 5);
    }
}