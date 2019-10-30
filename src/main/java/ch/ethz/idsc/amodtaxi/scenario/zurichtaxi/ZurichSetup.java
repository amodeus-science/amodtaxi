/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.io.File;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioSetup;

/* package */ enum ZurichSetup {
    ;

    public static void in(File workingDir) throws Exception {
        ZurichGeoInformation.setup();
        File resourcesDir = new File(Locate.repoFolder(ZurichSetup.class, "amodtaxi"), //
                "resources/zurichTaxiScenario");
        ScenarioSetup.in(workingDir, resourcesDir);
    }
}
