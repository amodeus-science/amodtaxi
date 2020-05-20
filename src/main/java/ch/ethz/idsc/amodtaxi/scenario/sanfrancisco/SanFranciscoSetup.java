/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioSetup;

/* package */ enum SanFranciscoSetup {
    ;

    public static void in(File workingDir) throws Exception {
        SanFranciscoGeoInformation.setup();
        try {
            File resourcesDir = new File(Locate.repoFolder(SanFranciscoScenarioCreation.class, "amodtaxi"), "src/main/resources/sanFranciscoScenario");
            ScenarioSetup.in(workingDir, resourcesDir);
        } catch (Exception e) {
            ScenarioSetup.in(workingDir, "sanFranciscoScenario");
        }
    }
}
