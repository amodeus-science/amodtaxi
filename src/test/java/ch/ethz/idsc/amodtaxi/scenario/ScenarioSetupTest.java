/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.tensor.io.DeleteDirectory;

public class ScenarioSetupTest {

    @Test
    public void test() throws Exception {
        /* Init */
        File workingDir = new File(Locate.repoFolder(ScenarioSetup.class, "amodtaxi"), //
                "test");
        File resourcesDir = new File(Locate.repoFolder(ScenarioSetup.class, "amodtaxi"), //
                "resources/chicagoScenario");
        Assert.assertTrue(workingDir.exists() || workingDir.mkdir());

        /* Run function of interest */
        ScenarioSetup.in(workingDir, resourcesDir);

        /* Check functionality */
        Assert.assertTrue(new File(workingDir, ScenarioLabels.config).exists());
        Assert.assertTrue(new File(workingDir, ScenarioLabels.pt2MatSettings).exists());
        Assert.assertTrue(new File(workingDir, ScenarioLabels.LPFile).exists());
        Assert.assertTrue(new File(workingDir, ScenarioLabels.amodeusFile).exists());

        /* Clean up */
        Assert.assertTrue(workingDir.exists());
        DeleteDirectory.of(workingDir, 2, 14);
        Assert.assertFalse(workingDir.exists());

    }
}
