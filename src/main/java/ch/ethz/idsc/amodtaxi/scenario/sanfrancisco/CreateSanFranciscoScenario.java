/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import ch.ethz.idsc.amodtaxi.scenario.ScenarioBasicNetworkPreparer;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodtaxi.osm.StaticMapCreator;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilterCollection;
import ch.ethz.idsc.amodtaxi.tripfilter.TripNetworkFilter;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class CreateSanFranciscoScenario {

    private static final Collection<LocalDate> dates = DateSelectSF.specific(06, 04);
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

    // TODO clean-up
    // TODO make naming consistent with Chicago
    public static void run(File dataDir, File processingDir, File destinDir) throws Exception {
        SanFranciscoSetup.in(processingDir);

        /** download of open street map data to create scenario */
        StaticMapCreator.now(processingDir);

        /** copy taxi trace files */
        System.out.println("dataDir: " + dataDir.getAbsolutePath());
        List<File> traceFiles = TraceFileChoice.getOrDefault(new File(dataDir, "cabspottingdata"), "new_").random(numTraceFiles);
        // List<File> traceFiles = //
        // TraceFileChoice.getOrDefault(new File(dataDir, "cabspottingdata"), "new_").specified("equioc", "onvahe", "epkiapme", "ippfeip");

        /** remove all links except car from network */
        Network network = ScenarioBasicNetworkPreparer.run(processingDir);
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);

        /** get dayTaxiRecord from trace files */
        FastLinkLookup fll = new FastLinkLookup(network, db);
        DayTaxiRecord dayTaxiRecord = ReadTraceFiles.in(fll, traceFiles, db);

        /** create scenario */
        HashMap<LocalDate, File> scenarioDirs = new HashMap<>();
        for (LocalDate localDate : dates) {
            try {
                /** compute scenario */

                TaxiTripFilterCollection speedEstimationTripFilter = new TaxiTripFilterCollection();
                /** trips which are faster than the network freeflow speeds would allow are removed */
                speedEstimationTripFilter.addFilter(new TripNetworkFilter(network, db, //
                        Quantity.of(2.235200008, "m*s^-1"), Quantity.of(3600, "s"), Quantity.of(200, "m"), true));

                TaxiTripFilterCollection finalPopulationTripFilter = new TaxiTripFilterCollection();
                /** trips which are faster than the network freeflow speeds would allow are removed */
                finalPopulationTripFilter.addFilter(new TripNetworkFilter(network, db, //
                        Quantity.of(2.235200008, "m*s^-1"), Quantity.of(3600, "s"), Quantity.of(200, "m"), false));

                StandaloneFleetConverterSF sfc = new StandaloneFleetConverterSF(processingDir, //
                        dayTaxiRecord, db, network, timeStep, timeConvert, speedEstimationTripFilter, //
                        finalPopulationTripFilter);
                sfc.run(localDate);

                /** copy scenario to new location */
                File destinDirDay = new File(destinDir, localDate.toString());
                destinDirDay.mkdirs();
                scenarioDirs.put(localDate, destinDirDay);
                try {
                    ScenarioAssemblerSF.copyFinishedScenario(processingDir.getAbsolutePath(), destinDirDay);
                } catch (Exception e) {
                    System.err.println("Failed to copy scenario for " + localDate);
                }
            } catch (Exception e) {
                System.err.println("Failed to create scenario for localDate " + localDate);
                e.printStackTrace();
            }
        }

        // /** test consistency of created scenarios with independent analysis */
        // TaxiData scenarioData = CreateSanFranciscoStaticAnalysis.runStaticAnalysis(destinDir, traceFiles, network, db);
    }
}
