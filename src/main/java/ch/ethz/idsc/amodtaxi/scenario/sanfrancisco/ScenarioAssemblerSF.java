/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import ch.ethz.idsc.tensor.io.DeleteDirectory;

/* package */ class ScenarioAssemblerSF {

    public static void copyFinishedScenario(String originDir, File destinDir) throws IOException {
        System.out.println("copying scenario from : " + originDir);
        System.out.println("copying  scenario  to : " + destinDir.getAbsolutePath());

        if (destinDir.exists())
            DeleteDirectory.of(destinDir, 6, 11000);
        destinDir.mkdir();

        CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES };

        String[] fileNames = new String[] { "AmodeusOptions.properties", "LPOptions.properties", "linkSpeedData", "network.xml.gz" };

        for (String fileName : fileNames) {
            Path source = Paths.get(originDir, fileName);
            Path target = Paths.get(destinDir.getAbsolutePath(), fileName);
            Files.copy(source, target, options);
        }

        /** population.xml */
        {
            Path source = Paths.get(originDir, "population.xml.gz");
            Path target = Paths.get(destinDir.getAbsolutePath(), "population.xml.gz");
            Files.copy(source, target, options);
        }

        /** config_full.xml */
        {
            Path source = Paths.get(originDir, "config_fullPublish.xml");
            Path target = Paths.get(destinDir.getAbsolutePath(), "config_full.xml");
            Files.copy(source, target, options);

        }
    }
}