/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodeus.util.io.MultiFileReader;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodtaxi.osm.StaticMapCreator;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilter;
import ch.ethz.idsc.amodtaxi.tripfilter.TripNetworkFilter;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

public class CreateSanFranciscoScenario {

    private static final Collection<LocalDate> dates = DateSelect.specific(06, 04);
    private static final int numTraceFiles = 536;// maximum taxis are: 536;
    private static final AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles"));
    private static final ReferenceFrame referenceFrame = SanFranciscoReferenceFrames.SANFRANCISCO;
    private static final Scalar timeStep = Quantity.of(10, "s");

    public static void main(String[] args) throws Exception {
        File dataDir = new File(args[0]);
        File workingDir = MultiFileTools.getDefaultWorkingDirectory();
        File processingDir = workingDir;
        File destinDir = workingDir;
        run(dataDir, processingDir, destinDir);
    }

    public static void run(File dataDir, File processingDir, File destinDir) throws Exception {
        SanFranciscoGeoInformation.setup();

        /** copy taxi trace files */
        System.out.println("dataDir: " + dataDir.getAbsolutePath());
        List<File> taxiFiles = new MultiFileReader(new File(dataDir, "cabspottingdata"), "new_").getFolderFiles();
        List<File> traceFiles = (new TraceFileChoice(taxiFiles)).random(numTraceFiles);
        // List<File> traceFiles = (new
        // TraceFileChoice(taxiFiles)).specified("equioc", "onvahe", "epkiapme",
        // "ippfeip");

        /** copy other scenario files */
        File settingsDir = new File(Locate.repoFolder(CreateSanFranciscoScenario.class, "amodtaxi"), "resources/sanFranciscoScenario");
        CopyFiles.now(settingsDir.getAbsolutePath(), processingDir.getAbsolutePath(), //
                Arrays.asList(new String[] { "AmodeusOptions.properties", "LPOptions.properties", "config_full.xml", //
                        "config_fullPublish.xml", "pt2matsim_settings.xml" }),
                true);

        /** download of open street map data to create scenario map using
         * pt2matsim */
        StaticMapCreator.now(processingDir, ScenarioLabels.osmData, ScenarioLabels.amodeusFile, ScenarioLabels.pt2MatSettings);

        /** remove all links except car from network */
        Network network = InitialNetworkPreparerSF.run(processingDir);
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);

        /** get dayTaxiRecord from trace files */
        // QuadTree<Link> qt = CreateQuadTree.of(network, db);
        FastLinkLookup qt = new FastLinkLookup(network, db);
        DayTaxiRecord dayTaxiRecord = ReadTraceFiles.in(qt, traceFiles, db);

        /** create scenario */
        HashMap<LocalDate, File> scenarioDirs = new HashMap<>();
        for (LocalDate localDate : dates) {
            try {
                /** compute scenario */

                TaxiTripFilter speedEstimationTripFilter = new TaxiTripFilter();
                /** trips which are faster than the network freeflow speeds would allow are removed */
                speedEstimationTripFilter.addFilter(new TripNetworkFilter(network, db, //
                        Quantity.of(5.5, "m*s^-1"), Quantity.of(3600, "s"), Quantity.of(200, "m"), true));

                TaxiTripFilter finalPopulationTripFilter = new TaxiTripFilter();
                /** trips which are faster than the network freeflow speeds would allow are removed */
                finalPopulationTripFilter.addFilter(new TripNetworkFilter(network, db, //
                        Quantity.of(2.5, "m*s^-1"), Quantity.of(7200, "s"), Quantity.of(200, "m"), false));

                StandaloneFleetConverterSF sfc = new StandaloneFleetConverterSF(processingDir, //
                        dayTaxiRecord, db, network, timeStep, timeConvert, speedEstimationTripFilter, //
                        finalPopulationTripFilter);
                sfc.run(localDate);

                /** copy scenario to new location */
                String destinDirDayStr = destinDir + "/" + localDate.toString();
                File destinDirDay = new File(destinDirDayStr);
                if (!destinDirDay.isDirectory())
                    destinDirDay.mkdirs();
                scenarioDirs.put(localDate, destinDirDay);
                try {
                    ScenarioAssemblerSF.copyFinishedScenario(processingDir.getAbsolutePath(), destinDirDay);
                } catch (Exception ex) {
                    System.err.println("failed to copy scenario for " + localDate);
                }
            } catch (Exception ex) {
                System.err.println("could not create scenario for localDate " + localDate);
                ex.printStackTrace();
            }
        }

        // /** test consistency of created scenarios with independent analysis */
        // TaxiData scenarioData = CreateSanFranciscoStaticAnalysis.runStaticAnalysis(destinDir, traceFiles, network, db);
    }
}
