package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;

import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;

public enum TestDirectories {
    ;

    public static final File WORKING = new File(MultiFileTools.getDefaultWorkingDirectory(), "test-scenario");
    public static final File CHICAGO = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/test/chicagoScenario");
    public static final File MINI = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/test/miniScenario");
    public static final File SAN_FRANCISCO = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/test/sanFranciscoScenario");
}
