/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.time.ZoneId;
import java.util.List;

import ch.ethz.idsc.amodtaxi.scenario.ScenarioBasicNetworkPreparer;
import ch.ethz.idsc.amodtaxi.scenario.data.TaxiData;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;

/* package */ class CreateSanFranciscoStaticAnalysis {
    private static final int numTraceFiles = 536; // 536;
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
        // List<File> taxiFiles = new MultiFileReader(new File(dataDir, "cabspottingdata"), "new_").getFolderFiles();
        // System.out.println("Found Taxi files: " + taxiFiles.size());
        List<File> traceFiles = TraceFileChoice.getOrDefault(new File(dataDir, "cabspottingdata"), "new_").random(numTraceFiles);

        /** remove all links except car from network */
        Network network = ScenarioBasicNetworkPreparer.run(dataDir);
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);

        runStaticAnalysis(processingDir, traceFiles, network, db);
    }

    public static TaxiData runStaticAnalysis(File destinDir, List<File> traceFiles, Network network, MatsimAmodeusDatabase db) throws Exception {
        /** test consistency of created scenarios with independent analysis */
        File staticAnalysis = new File(destinDir + "/staticAnalysis");
        staticAnalysis.mkdirs();
        return TaxiData.staticAnalyze(traceFiles, db, network, staticAnalysis, TaxiStampReaderSF.INSTANCE);
    }
}
