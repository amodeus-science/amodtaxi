package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.data;

import java.io.File;
import java.time.ZoneId;
import java.util.List;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.io.MultiFileReader;
import ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.InitialNetworkPreparerSF;
import ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.SanFranciscoGeoInformation;
import ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.SanFranciscoReferenceFrames;
import ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.TraceFileChoice;

public class CreateSanFranciscoStaticAnalysis {

    private static final int numTraceFiles = 536;// 536;
    private static final AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles"));
    private static final ReferenceFrame referenceFrame = SanFranciscoReferenceFrames.SANFRANCISCO;

    /** @param args = [dataDirectory, workingDirectory], meaning
     *            [raw data directory, directory for processing]
     * 
     *            The data directory must contain all needed information to create the San Francisco scenario, the
     *            processig Directory can be empty. On clruch's file system:
     * 
     *            /home/clruch/polybox/DataSets/SanFranciscoTaxi
     *            /home/clruch/Downloads/2018_06_07_SanFranciscoScenarioFinal
     * 
     * @throws Exception */
    public static void main(String[] args) throws Exception {
        File dataDir = new File(args[0]);
        File processingDir = new File(args[1]);
        run(dataDir, processingDir);
    }

    public static void run(File dataDir, File processingDir) throws Exception {
        SanFranciscoGeoInformation.setup();

        /** copy taxi trace files */
        System.out.println("dataDir: " + dataDir.getAbsolutePath());
        List<File> taxiFiles = new MultiFileReader(new File(dataDir, "cabspottingdata"), "new_").getFolderFiles();
        System.out.println("Found Taxi files: " + taxiFiles.size());
        List<File> traceFiles = (new TraceFileChoice(taxiFiles)).random(numTraceFiles);

        // TODO remove these comments...
        // List<File> traceFiles = (new TraceFileChoice(taxiFiles)).specified("equioc", "onvahe", "epkiapme", "ippfeip");

        /** copy other scenario files */
        // File settingsDir = new File(Locate.repoFolder(CreateSanFranciscoStaticAnalysis.class, "amodeus"), "resources/sanFranciscoScenario");
        // CopyFiles.now(settingsDir.getAbsolutePath(), processingDir.getAbsolutePath(), //
        // Arrays.asList(new String[] { "AmodeusOptions.properties", "av.xml", "config_full.xml", "config.xml", //
        // "config_fullPublish.xml", "pt2matsim_settings.xml" }),
        // true);

        /** remove all links except car from network */
        Network network = InitialNetworkPreparerSF.run(dataDir);
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);

        runStaticAnalysis(processingDir, traceFiles, network, db);
    }

    public static TaxiData runStaticAnalysis(File destinDir, List<File> traceFiles, Network network, //
            MatsimAmodeusDatabase db) throws Exception {
        /** test consistency of created scenarios with independent analysis */
        File staticAnalysis = new File(destinDir + "/staticAnalysis");
        if (!staticAnalysis.isDirectory()) {
            staticAnalysis.mkdirs();
        }
        TaxiData scenarioData = TaxiData.staticAnalyze(traceFiles, db, network, staticAnalysis, timeConvert);
        return scenarioData;
    }
}
