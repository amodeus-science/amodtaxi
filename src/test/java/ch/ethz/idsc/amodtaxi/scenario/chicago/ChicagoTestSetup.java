package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;

import ch.ethz.idsc.amodeus.util.io.GZHandler;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioSetup;
import ch.ethz.idsc.amodtaxi.scenario.TestDirectories;

/* package */ enum ChicagoTestSetup {
    ;

    public static void in(File workingDir) throws Exception {
        ChicagoGeoInformation.setup();
        ScenarioSetup.in(workingDir, TestDirectories.CHICAGO, ScenarioLabels.networkGz);
        GZHandler.extract(new File(workingDir, ScenarioLabels.networkGz), new File(workingDir, ScenarioLabels.network));
    }
}
