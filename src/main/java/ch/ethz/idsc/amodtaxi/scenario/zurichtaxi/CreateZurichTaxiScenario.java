/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import static ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.ZurichTaxiConstants.simualtionDate;

import java.io.File;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.taxitrip.ExportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.ImportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.CreateQuadTree;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.linkspeed.iterative.IterativeLinkSpeedEstimator;
import ch.ethz.idsc.amodtaxi.osm.OsmLoader;
import ch.ethz.idsc.amodtaxi.population.TripPopulationCreator;
import ch.ethz.idsc.amodtaxi.scenario.FinishedScenario;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioBasicNetworkPreparer;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilter;
import ch.ethz.idsc.amodtaxi.tripfilter.TripDurationFilter;
import ch.ethz.idsc.amodtaxi.tripfilter.TripNetworkFilter;
import ch.ethz.idsc.tensor.qty.Quantity;

/** Can be done:
 * - bigger map incl. St. Gallen
 * - more than 20k iterations
 * ... ? */
public class CreateZurichTaxiScenario {

    private final File workingDir;
    // private final File processingDir;
    private File finalTripsFile;
    private Network network = null;
    private MatsimAmodeusDatabase db = null;
    private final int maxIter = 20000;
    private final AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("Europe/Paris"));

    public CreateZurichTaxiScenario(File workingDir) throws Exception {
        this.workingDir = workingDir;
        ZurichSetup.in(workingDir);
        run();
        File destinDir = new File(workingDir, "CreatedScenario");
        Objects.requireNonNull(finalTripsFile);

        System.out.println("The final trips file is: ");
        System.out.println(finalTripsFile.getAbsolutePath());

        /** loading final trips */
        List<TaxiTrip> finalTrips = ImportTaxiTrips.fromFile(finalTripsFile);

        /** filtering the ones without meaningful duration */
        TaxiTripFilter finalTripFilter = new TaxiTripFilter();
        /** trips which are faster than the network freeflow speeds would allow are removed */
        finalTripFilter.addFilter(new TripNetworkFilter(network, db, //
                Quantity.of(0.0000001, "m*s^-1"), Quantity.of(100000, "s"), Quantity.of(0.000001, "m"), true));
        finalTripFilter.addFilter(new TripDurationFilter(Quantity.of(1, "s"), Quantity.of(Double.MAX_VALUE, "s")));
        List<TaxiTrip> fitForTrafficEstimation = finalTripFilter.filterStream(finalTrips.stream()).collect(Collectors.toList());
        finalTripFilter.printSummary();
        ExportTaxiTrips.toFile(fitForTrafficEstimation.stream(), new File(workingDir, "estimationTrips.csv"));

        new IterativeLinkSpeedEstimator(maxIter).compute(workingDir, network, db, fitForTrafficEstimation);

        FinishedScenario.copyToDir(workingDir.getAbsolutePath(), destinDir.getAbsolutePath(), //
                new String[] { "AmodeusOptions.properties", "network.xml.gz", "population.xml.gz", //
                        "LPOptions.properties", "config_full.xml", "linkSpeedData" });

    }

    private void run() throws Exception {
        // FIXME remove debug loop once done
        boolean debug = false;

        /** download of open street map data to create scenario */
        System.out.println("Downloading open stret map data, this may take a while...");
        File osmFile = new File(workingDir, ScenarioLabels.osmData);
        OsmLoader osmLoader = OsmLoader.of(new File(workingDir, ScenarioLabels.amodeusFile));
        osmLoader.saveIfNotAlreadyExists(osmFile);
        /** generate a network using pt2Matsim */
        if (!debug)
            Osm2MultimodalNetwork.run(workingDir.getAbsolutePath() + "/" + ScenarioLabels.pt2MatSettings);
        /** prepare the network */
        ScenarioBasicNetworkPreparer.run(workingDir);

        /** load taxi data from the trips file */
        File tripsFile = new File("/home/clruch/Downloads/tripsJune21_best_new.csv");
        ZurichTaxiTripReader reader = new ZurichTaxiTripReader(",");
        List<TaxiTrip> allTrips = reader.getTrips(tripsFile);
        allTrips.stream().forEach(t -> {
            System.out.println(t.toString());
        });

        // //
        // File processingdir = new File(workingDir, "Scenario");
        // if (processingdir.isDirectory())
        // DeleteDirectory.of(processingdir, 2, 25);
        // if (!processingdir.isDirectory())
        // processingdir.mkdir();
        // CopyFiles.now(workingDir.getAbsolutePath(), processingdir.getAbsolutePath(), //
        // Arrays.asList(new String[] { "AmodeusOptions.properties", "config_full.xml", //
        // "network.xml", "network.xml.gz", "LPOptions.properties" }));
        ScenarioOptions scenarioOptions = new ScenarioOptions(workingDir, //
                ScenarioOptionsBase.getDefault());
        // LocalDate simulationDate = LocalDateConvert.ofOptions(scenarioOptions.getString("date"));

        File configFile = new File(scenarioOptions.getPreparerConfigName());
        System.out.println(configFile.getAbsolutePath());
        GlobalAssert.that(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        network = NetworkLoader.fromNetworkFile(new File(workingDir, configFull.network().getInputFile()));
        db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());
        FastLinkLookup fll = new FastLinkLookup(network, db);

        // /** prepare for creation of scenario */
        // TaxiTripsReader tripsReader = new OnlineTripsReaderChicago();
        // TripBasedModifier tripModifier = new ChicagoOnlineTripBasedModifier(random, network, //
        // fll, new File(processingdir, "virtualNetworkChicago"));
        // TaxiTripFilter finalTripFilter = new TaxiTripFilter();
        // /** trips which are faster than the network freeflow speeds would allow are removed */
        // finalTripFilter.addFilter(new TripNetworkFilter(network, db, //
        // Quantity.of(2.235200008, "m*s^-1"), Quantity.of(3600, "s"), Quantity.of(200, "m"), true));
        //
        // // TODO eventually remove, this did not improve the fit.
        // // finalFilters.addFilter(new TripMaxSpeedFilter(network, db, ScenarioConstants.maxAllowedSpeed));
        // ChicagoOnlineTripFleetConverter converter = //
        // new ChicagoOnlineTripFleetConverter(scenarioOptions, network, tripModifier, //
        // new ChicagoFormatModifier(), finalTripFilter, tripsReader);

        TaxiTripFilter finalTripFilter = new TaxiTripFilter();

        // /** trips which are faster than the network freeflow speeds would allow are removed */
        // finalTripFilter.addFilter(new TripNetworkFilter(network, db, //
        // Quantity.of(2.235200008, "m*s^-1"), Quantity.of(3600, "s"), Quantity.of(200, "m"), true));

        QuadTree<Link> qt = CreateQuadTree.of(network);
        TripPopulationCreator populationCreator = //
                new TripPopulationCreator(workingDir, configFull, network, db, qt, //
                        simualtionDate, timeConvert, finalTripFilter);
        populationCreator.process(allTrips, new File(workingDir, "/finalTrips.csv"));
        finalTripsFile = populationCreator.getFinalTripFile();
    }

    // --
    public static void main(String[] args) throws Exception {
        File workingDir = new File(args[0]);
        new CreateZurichTaxiScenario(workingDir);
    }

}
