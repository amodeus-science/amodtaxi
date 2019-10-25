/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;

/** Tests if data for the creation of the Chicago taxi scenario is accessible from the
 * web API. */
public class ChicagoDataLoaderTest {

    @Test // TODO download a smaller file...
    public void test() throws Exception {
        File settingsDir = //
                new File(Locate.repoFolder(CreateChicagoScenario.class, "amodtaxi"), "resources/chicagoScenario");
        File tripFile = ChicagoDataLoader.from(ScenarioLabels.amodeusFile, settingsDir);
        boolean exists = tripFile.exists();
        boolean deleted = tripFile.delete();
        Assert.assertTrue(exists);
        Assert.assertTrue(deleted);
    }

}
