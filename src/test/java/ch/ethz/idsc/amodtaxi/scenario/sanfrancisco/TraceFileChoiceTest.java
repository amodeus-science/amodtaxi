package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.util.List;

import ch.ethz.idsc.amodeus.util.io.Locate;
import junit.framework.TestCase;

public class TraceFileChoiceTest extends TestCase {
    public void testGet() {
        File dataFile = new File(Locate.repoFolder(TraceFileChoiceTest.class, "amodtaxi"), "resources/sanFranciscoScenario/cabspottingdata");
        List<File> traceFiles = TraceFileChoice.get(dataFile, "new_").random(2);
        assertEquals(2, traceFiles.size());
    }

    public void testGetOrDefault() {
        List<File> traceFiles = TraceFileChoice.getOrDefault(new File("404"), "new_").random(2);
        assertEquals(2, traceFiles.size());
    }

    public void testFail() {
        try {
            TraceFileChoice.get(new File("404"), "new_");
            fail();
        } catch (RuntimeException e) {
            //
        }
    }
}
