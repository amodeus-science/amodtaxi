package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;
import java.util.Arrays;

import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.scenario.Pt2MatsimXML;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;

public enum ChicagoSetup {
    ;

    public static void in(File workingDir) throws Exception {
        ChicagoGeoInformation.setup();
        /** copy relevant files containing settings for scenario generation */
        File resourcesDir = new File(Locate.repoFolder(CreateChicagoScenario.class, "amodtaxi"), "resources/chicagoScenario");
        CopyFiles.now(resourcesDir.getAbsolutePath(), workingDir.getAbsolutePath(), //
                Arrays.asList(new String[] { ScenarioLabels.avFile, ScenarioLabels.config, //
                        ScenarioLabels.pt2MatSettings, ScenarioLabels.LPFile }),
                true);
        /** AmodeusOptions.properties is not replaced as it might be changed by user during
         * scenario generation process. */
        if (!new File(workingDir, ScenarioLabels.amodeusFile).exists())
            CopyFiles.now(resourcesDir.getAbsolutePath(), workingDir.getAbsolutePath(), //
                    Arrays.asList(new String[] { ScenarioLabels.amodeusFile }), false);
        Pt2MatsimXML.toLocalFileSystem(new File(workingDir, ScenarioLabels.pt2MatSettings), //
                workingDir.getAbsolutePath());
    }

}
