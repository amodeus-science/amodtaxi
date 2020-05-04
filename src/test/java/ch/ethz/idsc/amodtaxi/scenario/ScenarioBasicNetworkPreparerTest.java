/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.osm.OsmLoader;

public class ScenarioBasicNetworkPreparerTest {

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
        map.put("outputCoordinateSystem", "EPSG:21781"); // Set reference frame to Zurich
        Pt2MatsimXML.changeAttributes(ptFile, map);

        /* Download of open street map data to create scenario */
        System.out.println("Downloading open street map data, this may take a while...");
        File osmFile = new File(workingDir, ScenarioLabels.osmData);
        OsmLoader osmLoader = OsmLoader.of(new File(workingDir, ScenarioLabels.amodeusFile));
        osmLoader.saveIfNotAlreadyExists(osmFile);
        /** generate a network using pt2Matsim */
        System.out.println(workingDir.getAbsolutePath() + "/" + ScenarioLabels.pt2MatSettings);
        Osm2MultimodalNetwork.run(workingDir.getAbsolutePath() + "/" + ScenarioLabels.pt2MatSettings);
        /** prepare the network */

        /* Run function of interest */
        ScenarioBasicNetworkPreparer.run(workingDir);

        /* Check functionality */
        Assert.assertTrue(new File(workingDir + "/network.xml").exists());
        Assert.assertTrue(new File(workingDir + "/network.xml.gz").exists());

        /* Clean up */
        Assert.assertTrue(workingDir.exists());
        FileUtils.deleteDirectory(workingDir);
        Assert.assertFalse(workingDir.exists());
    }
}
