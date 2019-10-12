/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.analysis.Analysis;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.taxitrip.ExportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.CreateQuadTree;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.linkspeed.LinkSpeedsExport;
import ch.ethz.idsc.amodtaxi.linkspeed.iterative.IterativeLinkSpeedEstimator;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilter;
import ch.ethz.idsc.tensor.Scalar;

/* package */ class StandaloneFleetConverterSF {

    private final File workingDirectory;
    private final MatsimAmodeusDatabase db;
    private final Network network;
    private final File configFile;
    private final Config configFull;
    private final int maxIter = 100;
    private DayTaxiRecord dayTaxiRecord;
    private ScenarioOptions simOptions;
    private final TaxiTripFilter taxiTripFilter;

    private final QuadTree<Link> qt;
    private File outputDirectory;
    private final AmodeusTimeConvert timeConvert;

    private final Scalar TIME_STEP;

    public StandaloneFleetConverterSF(File workingDirectory, DayTaxiRecord dayTaxiRecord, //
            MatsimAmodeusDatabase db, Network network, Scalar TIME_STEP, //
            AmodeusTimeConvert timeConvert, TaxiTripFilter taxiTripFilter) throws Exception {
        this.workingDirectory = workingDirectory;
        this.dayTaxiRecord = dayTaxiRecord;
        this.db = db;
        this.TIME_STEP = TIME_STEP;
        this.timeConvert = timeConvert;
        this.taxiTripFilter = taxiTripFilter;
        simOptions = new ScenarioOptions(workingDirectory, ScenarioOptionsBase.getDefault());
        configFile = new File(simOptions.getPreparerConfigName());
        GlobalAssert.that(configFile.exists());
        configFull = ConfigUtils.loadConfig(configFile.toString());
        this.network = network;
        GlobalAssert.that(Objects.nonNull(network));
        qt = CreateQuadTree.of(network);
    }

    public void run(LocalDate simulationDate) throws Exception {

        /** STEP 0: Prepare Environment and load all configuration files */
        outputDirectory = StaticHelper.prepareFolder(workingDirectory, new File(workingDirectory, configFull.controler().getOutputDirectory()));

        /** STEP 1: Generate simobjs from daytaxirecords for visualization */
        SimulationFleetDumperSF sfd = new SimulationFleetDumperSF(db, network, TIME_STEP, qt, timeConvert);
        outputDirectory.mkdirs();
        GlobalAssert.that(outputDirectory.isDirectory());
        sfd.createDumpOf(dayTaxiRecord, outputDirectory, simulationDate);

        /** STEP 2: Find relevant trips */
        Collection<TaxiTrip> tripsAll = AllTaxiTrips.in(dayTaxiRecord).on(simulationDate);

        /** STEP 3: Filter unwanted trips */
        System.out.println("Trips before filtering: " + tripsAll.size());
        List<TaxiTrip> trips = taxiTripFilter.filterStream(tripsAll.stream()).collect(Collectors.toList());
        System.out.println("Trips after filtering:  " + trips.size());
        taxiTripFilter.printSummary();

        /** STEP 4: Export final taxi trips */
        ExportTaxiTrips.toFile(trips.stream(), new File(workingDirectory, "finalTrips.csv"));

        // taxiTripFilter.
        // ClosestLinkSelect linkSelect = new ClosestLinkSelect(db, qt);
        // AverageNetworkSpeed speedFilter = new AverageNetworkSpeed(network, linkSelect, simulationDate, timeConvert);
        // List<TaxiTrip> trips = tripsAll.stream().filter(t -> speedFilter.isBelow(t, maxAverageSpeed)).collect(Collectors.toList());

        /** STEP 4: Generate population.xml using the recordings */
        AdamAndEve.create(workingDirectory, trips, network, db, timeConvert, qt, simulationDate);

        /** STEP 3: Generate the report */
        try {
            Analysis analysis = Analysis.setup(simOptions, outputDirectory, network, db);
            analysis.run();
        } catch (Exception exception) {
            System.err.println("could not produce report, analysis failed.");
            exception.printStackTrace();
        }
        /** STEP 4: Create Link Speed Data File */
        try {
            // other tested options...
            // TaxiLinkSpeedEstimator lsCalc = new ConventionalLinkSpeedCalculator(db, dayTaxiRecord, simulationDate, network, timeConvert);
            // TaxiLinkSpeedEstimator lsCalc = new FlowLinkSpeed(trips, network, timeConvert, db, qt, simulationDate);
            // TaxiLinkSpeedEstimator lsCalc = new FlowTimeInvLinkSpeed(trips, network, db, GLPKLinOptDelayCalculator.INSTANCE);

            // iterative
            IterativeLinkSpeedEstimator lsCalc = new IterativeLinkSpeedEstimator(maxIter);
            lsCalc.compute(workingDirectory, network, db, trips);

            File linkSpeedsFile = new File(simOptions.getLinkSpeedDataName() + "");
            LinkSpeedsExport.using(linkSpeedsFile, lsCalc);//
        } catch (Exception exception) {
            System.err.println("could not generate link speed data...");
            exception.printStackTrace();
        }
    }
}