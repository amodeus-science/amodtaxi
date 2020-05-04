/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.matsim.api.core.v01.network.Network;
import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.network.LinkModes;
import ch.ethz.idsc.amodtaxi.osm.OsmLoader;

public class NetworkCutterUtilTest {

    @Test
    public void test() throws Exception {
        /* Init */
        File workingDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "test");
        File resourcesDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/chicagoScenario");
        File ptFile = new File(workingDir, ScenarioLabels.pt2MatSettings);
        Assert.assertTrue(workingDir.exists() || workingDir.mkdir());
        ScenarioSetup.in(workingDir, resourcesDir);

        /* Configure Amodeus file */
        File amodeusFile = new File(workingDir, ScenarioLabels.amodeusFile);
        amodeusFile.delete();
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(resourcesDir, ScenarioLabels.amodeusFile)));
        properties.setProperty("boundingBox", "{8.54673, 47.37397, 8.55252, 47.37916}"); // test box with area of ETH
        FileOutputStream out = new FileOutputStream(amodeusFile);
        properties.store(out, null);
        out.close();
        Assert.assertTrue(amodeusFile.exists());

        /* Configure Pt2Mat file */
        Assert.assertTrue(ptFile.exists());
        Map<String, String> map = new HashMap<String, String>();
        map.put("outputCoordinateSystem", "EPSG:21781");
        Pt2MatsimXML.changeAttributes(ptFile, map);

        /* download of open street map data to create scenario */
        System.out.println("Downloading open street map data, this may take a while...");
        File osmFile = new File(workingDir, ScenarioLabels.osmData);
        OsmLoader osmLoader = OsmLoader.of(new File(workingDir, ScenarioLabels.amodeusFile));
        osmLoader.saveIfNotAlreadyExists(osmFile);
        /* generate a network using pt2Matsim */
        System.out.println(workingDir.getAbsolutePath() + "/" + ScenarioLabels.pt2MatSettings);
        Osm2MultimodalNetwork.run(workingDir.getAbsolutePath() + "/" + ScenarioLabels.pt2MatSettings);

        /* load the pt2matsim network */
        Network networkpt2Matsim = NetworkLoader.fromNetworkFile(new File(workingDir, "networkPt2Matsim.xml.gz"));
        GlobalAssert.that(!networkpt2Matsim.getNodes().isEmpty());

        /* Run function of interest */
        LinkModes linkModes = new LinkModes("car");
        Network filteredNetwork = NetworkCutterUtils.modeFilter(networkpt2Matsim, linkModes);

        /* Check functionality */
        Assert.assertTrue(filteredNetwork.getLinks().values().stream().allMatch(link -> link.getAllowedModes().contains("car")));

        /* Clean up */
        // Assert.assertTrue(workingDir.exists());
        // FileUtils.deleteDirectory(workingDir);
        // Assert.assertFalse(workingDir.exists());
    }
}
