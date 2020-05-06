/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.osm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.Properties;

import junit.framework.TestCase;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.tensor.io.DeleteDirectory;

public class OsmLoaderTest extends TestCase {
    public void test() throws Exception {
        /* Init */
        File workingDir = new File(Locate.repoFolder(OsmLoader.class, "amodtaxi"), "test");
        File resourcesDir = new File(Locate.repoFolder(OsmLoader.class, "amodtaxi"), "resources/chicagoScenario");
        File amodeusFile = new File(workingDir, ScenarioLabels.amodeusFile);
        assertTrue(workingDir.exists() || workingDir.mkdir());
        File osmFile = new File(workingDir, ScenarioLabels.osmData);
        assertFalse(osmFile.exists());

        /* Reduce boundingbox size in Properties */
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(new File(resourcesDir, ScenarioLabels.amodeusFile))) {
            properties.load(fis);
        }
        properties.setProperty("boundingBox", "{8.54773, 47.37697, 8.54952, 47.37916}"); // test box with area of ETH
        try (FileOutputStream out = new FileOutputStream(amodeusFile)) {
            properties.store(out, null);
        }
        assertTrue(amodeusFile.exists());

        /* Run function of interest */
        OsmLoader osmLoader = OsmLoader.of(new File(workingDir, ScenarioLabels.amodeusFile));
        osmLoader.saveIfNotAlreadyExists(osmFile);

        /* Check functionality */
        try ( //
                FileReader fileReader = new FileReader(osmFile);
                BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            assertTrue(bufferedReader.lines().anyMatch(line -> line.contains("ETH/Universitätsspital")));
        }

        /* Clean up */
        assertTrue(workingDir.exists());
        DeleteDirectory.of(workingDir, 2, 14);
        assertFalse(workingDir.exists());
    }
}
