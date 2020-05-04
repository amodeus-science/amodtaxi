/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;

import com.google.common.io.Files;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.network.LinkModes;
import ch.ethz.idsc.tensor.io.DeleteDirectory;

public class NetworkCutterUtilTest {

    @Test
    public void test() throws Exception {
        /* Init */
        File workingDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "test");
        File resourcesDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/testScenario");
        Assert.assertTrue(workingDir.exists() || workingDir.mkdir());
        ScenarioSetup.in(workingDir, resourcesDir);

        /* Using test open street map data to create scenario */
        System.out.println("Using map.osm from testScenario");
        File osmFile = new File(workingDir, ScenarioLabels.osmData);
        Files.copy(new File(resourcesDir, ScenarioLabels.osmData), osmFile);

        /* Load the pt2matsim network */
        Files.copy(new File(resourcesDir, "networkPt2Matsim.xml.gz"), new File(workingDir, "networkPt2Matsim.xml.gz"));
        Network networkpt2Matsim = NetworkLoader.fromNetworkFile(new File(workingDir, "networkPt2Matsim.xml.gz"));

        /* Run function of interest */
        LinkModes linkModes = new LinkModes("car");
        Network filteredNetwork = NetworkCutterUtils.modeFilter(networkpt2Matsim, linkModes);

        /* Check functionality */
        Assert.assertTrue(filteredNetwork.getLinks().values().stream().allMatch(link -> link.getAllowedModes().contains("car")));

        /* Clean up */
        Assert.assertTrue(workingDir.exists());
        DeleteDirectory.of(workingDir, 2, 14);
        Assert.assertFalse(workingDir.exists());
    }
}
