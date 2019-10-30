/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.util.Arrays;

import ch.ethz.idsc.amodeus.util.io.CopyFiles;

public enum ScenarioSetup {
    ;

    public static void in(File workingDir, File resourcesDir) throws Exception {

        /** copy relevant files containing settings for scenario generation */
        CopyFiles.now(resourcesDir.getAbsolutePath(), workingDir.getAbsolutePath(), //
                Arrays.asList(new String[] { ScenarioLabels.config, //
                        ScenarioLabels.pt2MatSettings, ScenarioLabels.LPFile }),
                true);

        /** AmodeusOptions.properties is not replaced as it might be changed by user during
         * scenario generation process. */
        if (!new File(workingDir, ScenarioLabels.amodeusFile).exists())
            CopyFiles.now(resourcesDir.getAbsolutePath(), workingDir.getAbsolutePath(), //
                    Arrays.asList(new String[] { ScenarioLabels.amodeusFile }), false);

        /** change pt2Matsim settings to local file system */
        Pt2MatsimXML.toLocalFileSystem(new File(workingDir, ScenarioLabels.pt2MatSettings), //
                workingDir.getAbsolutePath());
    }

}
