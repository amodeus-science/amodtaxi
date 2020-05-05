/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.taxitrip.ImportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.fleetconvert.TripFleetConverter;
import ch.ethz.idsc.amodtaxi.linkspeed.iterative.IterativeLinkSpeedEstimator;
import ch.ethz.idsc.amodtaxi.osm.StaticMapCreator;
import ch.ethz.idsc.amodtaxi.scenario.FinishedScenario;
import ch.ethz.idsc.amodtaxi.scenario.Scenario;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioBasicNetworkPreparer;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;
import ch.ethz.idsc.amodtaxi.trace.ReadTraceFiles;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilterCollection;
import ch.ethz.idsc.amodtaxi.tripfilter.TripNetworkFilter;
import ch.ethz.idsc.amodtaxi.tripmodif.RenumerationModifier;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifier;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifierCollection;
import ch.ethz.idsc.tensor.io.DeleteDirectory;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class CreateSanFranciscoScenario {
    private static final Collection<LocalDate> DATES = DateSelectSF.specific(6, 4);
    private static final int NUM_TRACE_FILES = 536; // maximum taxis are: 536;
    private static final AmodeusTimeConvert TIME_CONVERT = new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles"));
    private static final int MAX_ITER = 100_000;

    public static void main(String[] args) throws Exception {
        File dataDir = new File(args[0]);
        run(dataDir, MultiFileTools.getDefaultWorkingDirectory());
    }

    public static void run(File dataDir, File workingDir) throws Exception {
        SanFranciscoSetup.in(workingDir);

        /** download of open street map data to create scenario */
        StaticMapCreator.now(workingDir);

        /** copy taxi trace files */
        System.out.println("dataDir: " + dataDir.getAbsolutePath());
        List<File> traceFiles = TraceFileChoice.getOrDefault(new File(dataDir, "cabspottingdata"), "new_").random(NUM_TRACE_FILES);
        // List<File> traceFiles = //
        // TraceFileChoice.getOrDefault(new File(dataDir, "cabspottingdata"), "new_").specified("equioc", "onvahe", "epkiapme", "ippfeip");

        /** prepare the network */
        ScenarioBasicNetworkPreparer.run(workingDir);

        File processingDir = new File(workingDir, "Scenario");
        if (processingDir.isDirectory())
            DeleteDirectory.of(processingDir, 2, 25);
        processingDir.mkdir();
        CopyFiles.now(workingDir.getAbsolutePath(), processingDir.getAbsolutePath(), //
                Arrays.asList(ScenarioLabels.amodeusFile, ScenarioLabels.config, ScenarioLabels.network, ScenarioLabels.networkGz, ScenarioLabels.LPFile));

        /** based on the taxi data, create a population and assemble a AMoDeus scenario */
        ScenarioOptions scenarioOptions = new ScenarioOptions(processingDir, ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        System.out.println(configFile.getAbsolutePath());
        GlobalAssert.that(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        final Network network = NetworkLoader.fromNetworkFile(new File(processingDir, configFull.network().getInputFile()));
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());
        FastLinkLookup fll = new FastLinkLookup(network, db);

        /** get dayTaxiRecord from trace files */
        CsvFleetReaderSF reader = new CsvFleetReaderSF(new DayTaxiRecordSF(fll));
        DayTaxiRecord dayTaxiRecord = ReadTraceFiles.in(traceFiles, reader);

        /** create scenario */
        for (LocalDate localDate : DATES) {
            // try {
            // /** compute scenario */
            //
            // TaxiTripFilterCollection speedEstimationTripFilter = new TaxiTripFilterCollection();
            // /** trips which are faster than the network freeflow speeds would allow are removed */
            // speedEstimationTripFilter.addFilter(new TripNetworkFilter(network, db, //
            // Quantity.of(2.235200008, "m*s^-1"), Quantity.of(3600, "s"), Quantity.of(200, "m"), true));
            //
            // TaxiTripFilterCollection finalPopulationTripFilter = new TaxiTripFilterCollection();
            // /** trips which are faster than the network freeflow speeds would allow are removed */
            // finalPopulationTripFilter.addFilter(new TripNetworkFilter(network, db, //
            // Quantity.of(2.235200008, "m*s^-1"), Quantity.of(3600, "s"), Quantity.of(200, "m"), false));
            //
            // StandaloneFleetConverterSF sfc = new StandaloneFleetConverterSF(processingDir, //
            // dayTaxiRecord, db, network, timeStep, TIME_CONVERT, speedEstimationTripFilter, //
            // finalPopulationTripFilter);
            // sfc.run(localDate);
            //
            // /** copy scenario to new location */
            // File destinDirDay = new File(destinDir, localDate.toString());
            // destinDirDay.mkdirs();
            // try {
            // ScenarioAssemblerSF.copyFinishedScenario(processingDir.getAbsolutePath(), destinDirDay);
            // } catch (Exception e) {
            // System.err.println("Failed to copy scenario for " + localDate);
            // }
            // } catch (Exception e) {
            // System.err.println("Failed to create scenario for localDate " + localDate);
            // e.printStackTrace();
            // }
            /** prepare for creation of scenario */
            TaxiDataModifier tripModifier;
            {
                TaxiDataModifierCollection taxiDataModifierCollection = new TaxiDataModifierCollection();
                /** each taxi uses enumerates its requests individually causing duplicate local ids */
                taxiDataModifierCollection.addModifier(new RenumerationModifier());
                tripModifier = taxiDataModifierCollection;
            }
            TaxiTripFilterCollection tripFilter = new TaxiTripFilterCollection();
            /** trips which are faster than the network freeflow speeds would allow are removed */
            tripFilter.addFilter(new TripNetworkFilter(network, db, //
                    Quantity.of(2.235200008, SI.VELOCITY), Quantity.of(3600, SI.SECOND), Quantity.of(200, SI.METER), true));

            File destinDir = new File(workingDir, localDate.toString());
            List<TaxiTrip> finalTrips;
            { // prepare final scenario
                TripFleetConverter converter = new SanFranciscoTripFleetConverter( //
                        scenarioOptions, network, dayTaxiRecord, localDate, tripModifier, tripFilter, new File(processingDir, "tripData"));
                File finalTripsFile = Objects.requireNonNull(Scenario.create(workingDir, converter, processingDir, localDate, TIME_CONVERT));

                System.out.println("The final trips file is: " + finalTripsFile.getAbsolutePath());

                /** loading final trips */
                finalTrips = ImportTaxiTrips.fromFile(finalTripsFile);
            }
            if (MAX_ITER > 0)
                new IterativeLinkSpeedEstimator(MAX_ITER, new Random()).compute(processingDir, network, db, finalTrips);

            FinishedScenario.copyToDir(processingDir.getAbsolutePath(), destinDir.getAbsolutePath(), //
                    new String[] { //
                            ScenarioLabels.amodeusFile, ScenarioLabels.networkGz, ScenarioLabels.populationGz, //
                            ScenarioLabels.LPFile, ScenarioLabels.config, ScenarioLabels.linkSpeedData });
        }

        // /** test consistency of created scenarios with independent analysis */
        // TaxiData scenarioData = CreateSanFranciscoStaticAnalysis.runStaticAnalysis(destinDir, traceFiles, network, db);
    }
}
