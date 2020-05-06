package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioSetup;
import ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.SanFranciscoGeoInformation;

/* package */ enum ChicagoTestSetup {
    ;

    public static void in(File workingDir) throws Exception {
        SanFranciscoGeoInformation.setup();
        File resourcesDir = new File(Locate.repoFolder(CreateChicagoScenario.class, "amodtaxi"), //
                "resources/test/chicagoScenario");
        ScenarioSetup.in(workingDir, resourcesDir, ScenarioLabels.osmData);
    }
}
