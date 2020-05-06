/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.util.Collections;

import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.network.LinkModes;
import ch.ethz.idsc.tensor.io.DeleteDirectory;

public class NetworkCutterUtilTest {
    private static final File WORKING_DIR = new File(Locate.repoFolder(NetworkCutterUtils.class, "amodtaxi"), "test-scenario");
    private static final File RESOURCE_DIR = new File(Locate.repoFolder(NetworkCutterUtils.class, "amodtaxi"), "resources/test/miniScenario");

    @BeforeClass
    public static void setup() throws Exception {
        WORKING_DIR.mkdir();
        CopyFiles.now(RESOURCE_DIR.getAbsolutePath(), WORKING_DIR.getAbsolutePath(), //
                Collections.singletonList("networkPt2Matsim.xml.gz"), true);
    }

    @Test
    public void test() {
        Network networkpt2Matsim = NetworkLoader.fromNetworkFile(new File(WORKING_DIR, "networkPt2Matsim.xml.gz"));
        int allLinks = networkpt2Matsim.getLinks().size();

        /* Run function of interest */
        LinkModes linkModes = new LinkModes("car");
        Network filteredNetwork = NetworkCutterUtils.modeFilter(networkpt2Matsim, linkModes);
        int filteredLinks = filteredNetwork.getLinks().size();

        /* Check functionality */
        Assert.assertTrue(filteredNetwork.getLinks().values().stream().allMatch(link -> link.getAllowedModes().contains("car")));
        Assert.assertTrue(allLinks > filteredLinks);
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        DeleteDirectory.of(WORKING_DIR, 2, 2);
    }
}
