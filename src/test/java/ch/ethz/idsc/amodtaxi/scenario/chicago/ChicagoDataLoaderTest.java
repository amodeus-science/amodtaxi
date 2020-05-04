/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;

/** Tests if data for the creation of the Chicago taxi scenario is accessible from the
 * web API. */
public class ChicagoDataLoaderTest {

    @Test
    public void test() throws Exception {
        File settingsDir = new File(Locate.repoFolder(CreateChicagoScenario.class, "amodtaxi"), "resources/chicagoScenario");

        /* Reduce population size in Properties */
        String smallProp = "Manipulated_" + ScenarioLabels.amodeusFile;
        File smallPropFile = new File(settingsDir, smallProp);
        Properties properties = new Properties();
        properties.load(new FileInputStream(new File(settingsDir, ScenarioLabels.amodeusFile)));
        properties.setProperty(ScenarioOptionsBase.MAXPOPULATIONSIZEIDENTIFIER, "100");
        FileOutputStream out = new FileOutputStream(smallPropFile);
        properties.store(out, null);
        out.close();

        /* Check ChicagoDataLoader */
        File tripFile = ChicagoDataLoader.from(smallProp, settingsDir);
        Assert.assertTrue(tripFile.exists());

        /* Clean up */
        Assert.assertTrue(tripFile.delete());
        Assert.assertTrue(smallPropFile.delete());
    }
}
