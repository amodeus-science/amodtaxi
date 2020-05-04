/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioSetup;

/* package */ enum SanFranciscoSetup {
    ;

    public static void in(File workingDir) throws Exception {
        SanFranciscoGeoInformation.setup();
        File resourcesDir = new File(Locate.repoFolder(CreateSanFranciscoScenario.class, "amodtaxi"), //
                "resources/sanFranciscoScenario");
        ScenarioSetup.in(workingDir, resourcesDir, "config_fullPublish.xml");
    }
}
