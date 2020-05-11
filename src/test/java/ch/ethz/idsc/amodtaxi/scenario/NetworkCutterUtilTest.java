/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.util.Collections;

import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.util.network.LinkModes;
import ch.ethz.idsc.tensor.io.DeleteDirectory;

public class NetworkCutterUtilTest {
    @BeforeClass
    public static void setup() throws Exception {
        GlobalAssert.that(TestDirectories.WORKING.mkdirs());
        CopyFiles.now(TestDirectories.MINI.getAbsolutePath(), TestDirectories.WORKING.getAbsolutePath(), //
                Collections.singletonList("networkPt2Matsim.xml.gz"), true);
    }

    @Test
    public void test() {
        Network networkpt2Matsim = NetworkLoader.fromNetworkFile(new File(TestDirectories.WORKING, "networkPt2Matsim.xml.gz"));
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
        DeleteDirectory.of(TestDirectories.WORKING, 2, 2);
    }
}
