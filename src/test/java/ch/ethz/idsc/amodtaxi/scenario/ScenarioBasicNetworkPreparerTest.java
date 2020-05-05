/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.tensor.io.DeleteDirectory;

public class ScenarioBasicNetworkPreparerTest {

    @Test
    public void test() throws Exception {
        /* Init */
        File workingDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "test");
        File resourcesDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/testScenario");
        Assert.assertTrue(workingDir.exists() || workingDir.mkdir());
        ScenarioSetup.in(workingDir, resourcesDir);
        Files.copy(new File(resourcesDir, "networkPt2Matsim.xml.gz"), new File(workingDir, "networkPt2Matsim.xml.gz"));

        /* Run function of interest */
        ScenarioBasicNetworkPreparer.run(workingDir);

        /* Check functionality */
        Assert.assertTrue(new File(workingDir, ScenarioLabels.network).exists());
        Assert.assertTrue(new File(workingDir, ScenarioLabels.networkGz).exists());

        /* Clean up */
        Assert.assertTrue(workingDir.exists());
        DeleteDirectory.of(workingDir, 2, 14);
        Assert.assertFalse(workingDir.exists());
    }
}