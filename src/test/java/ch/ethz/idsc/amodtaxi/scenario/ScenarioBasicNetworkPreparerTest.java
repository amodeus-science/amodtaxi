/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.util.Arrays;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.tensor.io.DeleteDirectory;
import org.matsim.api.core.v01.network.Network;

public class ScenarioBasicNetworkPreparerTest {
    private static final File WORKING_DIR = new File(Locate.repoFolder(ScenarioBasicNetworkPreparer.class, "amodtaxi"), "test-scenario");
    private static final File RESOURCE_DIR = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/test/miniScenario");

    @BeforeClass
    public static void setup() throws Exception {
        WORKING_DIR.mkdir();

        CopyFiles.now(RESOURCE_DIR.getAbsolutePath(), WORKING_DIR.getAbsolutePath(), //
                Arrays.asList(ScenarioLabels.pt2MatSettings, "networkPt2Matsim.xml.gz"), true);
        /** change pt2Matsim settings to local file system */
        Pt2MatsimXML.toLocalFileSystem(new File(WORKING_DIR, ScenarioLabels.pt2MatSettings), WORKING_DIR.getAbsolutePath());
    }

    @Test
    public void test() {
        /* Run function of interest */
        ScenarioBasicNetworkPreparer.run(WORKING_DIR);

        /* Check functionality */
        Assert.assertTrue(new File(WORKING_DIR, ScenarioLabels.network).exists());
        Assert.assertTrue(new File(WORKING_DIR, ScenarioLabels.networkGz).exists());

        Network network = NetworkLoader.fromNetworkFile(new File(WORKING_DIR, ScenarioLabels.networkGz));
        Assert.assertFalse(network.getLinks().isEmpty());
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        DeleteDirectory.of(WORKING_DIR, 2, 5);
    }
}