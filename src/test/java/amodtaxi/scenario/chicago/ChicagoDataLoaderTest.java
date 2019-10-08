package amodtaxi.scenario.chicago;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.amodeus.util.io.Locate;

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
