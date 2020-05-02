/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.osm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;

public class OsmLoaderTest {

    @Test
    public void test() throws Exception {
        /* Init */
        File workingDir = new File(Locate.repoFolder(OsmLoader.class, "amodtaxi"), "test");
        File resourcesDir = new File(Locate.repoFolder(OsmLoader.class, "amodtaxi"), "resources/chicagoScenario");
        File amodeusFile = new File(workingDir, ScenarioLabels.amodeusFile);
        Assert.assertTrue(workingDir.exists() || workingDir.mkdir());
        File osmFile = new File(workingDir, ScenarioLabels.osmData);
        Assert.assertFalse(osmFile.exists());

        /* Reduce boundingbox size in Properties */
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(resourcesDir, ScenarioLabels.amodeusFile)));
        properties.setProperty("boundingBox", "{8.54673, 47.37397, 8.55252, 47.37916}"); // test box with area of ETH
        FileOutputStream out = new FileOutputStream(amodeusFile);
        properties.store(out, null);
        out.close();
        Assert.assertTrue(amodeusFile.exists());

        /* Run function of interest */
        OsmLoader osmLoader = OsmLoader.of(new File(workingDir, ScenarioLabels.amodeusFile));
        osmLoader.saveIfNotAlreadyExists(osmFile);

        /* Check functionality */
        FileReader fileReader = new FileReader(osmFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        Assert.assertTrue(bufferedReader.lines().anyMatch(line -> line.contains("ETH/Universitätsspital")));

        /* Clean up */
        bufferedReader.close();
        fileReader.close();
        Assert.assertTrue(workingDir.exists());
        FileUtils.deleteDirectory(workingDir);
        Assert.assertFalse(workingDir.exists());
    }
}
