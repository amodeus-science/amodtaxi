/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import ch.ethz.idsc.amodtaxi.fleetconvert.SanFranciscoTripFleetConverter;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioCreation;
import ch.ethz.idsc.amodtaxi.scenario.data.StaticAnalysis;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.io.MultiFileTools;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.fleetconvert.TripFleetConverter;
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

public class SanFranciscoScenarioCreation extends ScenarioCreation {
    private static final Collection<LocalDate> DATES = DateSelectSF.specific(6, 4);
    private static final int NUM_TRACE_FILES = 536; // maximum taxis are: 536;
    private static final AmodeusTimeConvert TIME_CONVERT = new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles"));

    public static void main(String[] args) throws Exception {
        File dataDir = args.length > 0 ? new File(args[0]) : null;
        dataDir = (Objects.nonNull(dataDir) && dataDir.isDirectory()) ? dataDir : null;
        SanFranciscoScenarioCreation scenario = SanFranciscoScenarioCreation.of(MultiFileTools.getDefaultWorkingDirectory(), dataDir).get(0);
        // List<File> traceFiles = TraceFileChoice.getOrDefault(new File(dataDir, "cabspottingdata"), "new_") //
        //         .specified("equioc", "onvahe", "epkiapme", "ippfeip");
        // SanFranciscoScenarioCreation scenario = SanFranciscoScenarioCreation.of(MultiFileTools.getDefaultWorkingDirectory(), traceFiles, DATES);
        scenario.linkSpeedData(100_000);
        StaticAnalysis staticAnalysis = scenario.staticAnalysis();
        staticAnalysis.saveTo(new File(MultiFileTools.getDefaultWorkingDirectory(), "static_analysis"));
    }

    // ---

    public static List<SanFranciscoScenarioCreation> of(File workingDirectory, File dataDirectory) throws Exception {
        return of(workingDirectory, dataDirectory, NUM_TRACE_FILES, DATES);
    }

    public static List<SanFranciscoScenarioCreation> of(File workingDirectory, File dataDirectory, Collection<LocalDate> dates) throws Exception {
        return of(workingDirectory, dataDirectory, NUM_TRACE_FILES, dates);
    }

    public static List<SanFranciscoScenarioCreation> of(File workingDirectory, File dataDirectory, int numTraceFiles) throws Exception {
        return of(workingDirectory, dataDirectory, numTraceFiles, DATES);
    }

    public static List<SanFranciscoScenarioCreation> of(File workingDirectory, File dataDirectory, int numTraceFiles, Collection<LocalDate> dates) throws Exception {
        List<File> traceFiles;
        if (Objects.nonNull(dataDirectory)) {
            System.out.println("data directory: " + dataDirectory.getAbsolutePath());
            traceFiles = TraceFileChoice.getOrDefault(new File(dataDirectory, "cabspottingdata"), "new_").random(numTraceFiles);
        } else {
            System.out.println("no data directory provided");
            traceFiles = TraceFileChoice.getDefault().random(NUM_TRACE_FILES);
        }
        return of(workingDirectory, traceFiles, dates);
    }

    public static List<SanFranciscoScenarioCreation> of(File workingDirectory, Collection<File> traceFiles) throws Exception {
        return of(workingDirectory, traceFiles, DATES);
    }

    public static List<SanFranciscoScenarioCreation> of(File workingDirectory, Collection<File> traceFiles, Collection<LocalDate> dates) throws Exception {
        SanFranciscoSetup.in(workingDirectory);

        /** download of open street map data to create scenario */
        StaticMapCreator.now(workingDirectory);

        /** prepare the network */
        ScenarioBasicNetworkPreparer.run(workingDirectory);

        File processingDir = new File(workingDirectory, "Scenario");
        if (processingDir.isDirectory())
            DeleteDirectory.of(processingDir, 2, 25);
        processingDir.mkdir();
        CopyFiles.now(workingDirectory.getAbsolutePath(), processingDir.getAbsolutePath(), //
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
        List<SanFranciscoScenarioCreation> scenarios = new ArrayList<>();
        for (LocalDate localDate : dates) {
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

            File destinDir = new File(workingDirectory, localDate.toString());
            File finalTripsFile;
            { // prepare final scenario
                TripFleetConverter converter = new SanFranciscoTripFleetConverter( //
                        scenarioOptions, network, dayTaxiRecord, localDate, tripModifier, tripFilter, new File(processingDir, "tripData"));
                finalTripsFile = Objects.requireNonNull(Scenario.create(workingDirectory, converter, processingDir, localDate, TIME_CONVERT));

                System.out.println("The final trips file is: " + finalTripsFile.getAbsolutePath());
            }

            FinishedScenario.copyToDir(processingDir.getAbsolutePath(), destinDir.getAbsolutePath(), //
                    ScenarioLabels.amodeusFile, ScenarioLabels.networkGz, ScenarioLabels.populationGz, //
                    ScenarioLabels.LPFile, ScenarioLabels.config);

            scenarios.add(new SanFranciscoScenarioCreation(network, db, fll, traceFiles, finalTripsFile, destinDir));
        }

        if (TraceFileChoice.DEFAULT_DATA.exists())
            DeleteDirectory.of(TraceFileChoice.DEFAULT_DATA, 1, 5);

        return scenarios;
    }

    // ---

    private final FastLinkLookup fastLinkLookup;
    private final Collection<File> traceFiles;

    private SanFranciscoScenarioCreation(Network network, MatsimAmodeusDatabase db, FastLinkLookup fastLinkLookup, //
            Collection<File> traceFiles, File taxiTripsFile, File directory) {
        super(network, db, taxiTripsFile, directory);
        this.fastLinkLookup = fastLinkLookup;
        this.traceFiles = traceFiles;
    }

    /** test consistency of created scenarios with independent analysis */
    public StaticAnalysis staticAnalysis() {
        StaticAnalysis staticAnalysis = new StaticAnalysis(fastLinkLookup, network, TaxiStampReaderSF.INSTANCE);
        staticAnalysis.of(traceFiles);
        return staticAnalysis;
    }
}
