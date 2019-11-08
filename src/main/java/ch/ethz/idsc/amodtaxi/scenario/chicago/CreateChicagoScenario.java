/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.taxitrip.ImportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.fleetconvert.ChicagoOnlineTripFleetConverter;
import ch.ethz.idsc.amodtaxi.linkspeed.iterative.IterativeLinkSpeedEstimator;
import ch.ethz.idsc.amodtaxi.osm.OsmLoader;
import ch.ethz.idsc.amodtaxi.scenario.FinishedScenario;
import ch.ethz.idsc.amodtaxi.scenario.Scenario;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioBasicNetworkPreparer;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsReader;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilter;
import ch.ethz.idsc.amodtaxi.tripfilter.TripNetworkFilter;
import ch.ethz.idsc.amodtaxi.tripmodif.ChicagoFormatModifier;
import ch.ethz.idsc.amodtaxi.tripmodif.ChicagoOnlineTripBasedModifier;
import ch.ethz.idsc.amodtaxi.tripmodif.TripBasedModifier;
import ch.ethz.idsc.tensor.io.DeleteDirectory;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class CreateChicagoScenario {

    /** in @param args[0] working directory (empty directory), this main function will create
     * an AMoDeus scenario based on the Chicago taxi dataset available online.
     * Settings can afterwards be changed in the AmodeusOptions.properties file located
     * in the directory.
     * 
     * @throws Exception */
    public static void main(String[] args) throws Exception {
        File workingDir = new File(args[0]);
        new CreateChicagoScenario(workingDir);
    }

    // --
    private static final AmodeusTimeConvert timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Chicago"));
    private static final Random random = new Random(123);
    private final File workingDir;
    private final File processingDir;
    private File finalTripsFile;
    private Network network = null;
    private MatsimAmodeusDatabase db = null;
    private final int maxIter = 100000;

    /** Possible ways to adapt:
     * - use recorded distance (miles?) for the analysis and the reshuffling of trips
     * - use not only community are abut also census tract. */
    private CreateChicagoScenario(File workingDir) throws Exception {
        this.workingDir = workingDir;
        ChicagoSetup.in(workingDir);
        processingDir = run();
        File destinDir = new File(workingDir, "CreatedScenario");
        Objects.requireNonNull(finalTripsFile);

        System.out.println("The final trips file is: ");
        System.out.println(finalTripsFile.getAbsolutePath());

        // this is the old LP-based code
        // ChicagoLinkSpeeds.compute(processingDir, finalTripsFile);
        // new code

        /** loading final trips */
        List<TaxiTrip> finalTrips = ImportTaxiTrips.fromFile(finalTripsFile).collect(Collectors.toList());
        new IterativeLinkSpeedEstimator(maxIter).compute(processingDir, network, db, finalTrips);

        FinishedScenario.copyToDir(processingDir.getAbsolutePath(), //
                destinDir.getAbsolutePath(), new String[] { //
                        "AmodeusOptions.properties", "network.xml.gz", "population.xml.gz", //
                        "LPOptions.properties", "config_full.xml", //
                        "virtualNetworkChicago", "linkSpeedData" });
        cleanUp(workingDir);
    }

    private File run() throws Exception {
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

        /** load taxi data for the city of Chicago */
        File tripFile;
        if (!debug) {
            tripFile = ChicagoDataLoader.from(ScenarioLabels.amodeusFile, workingDir);
        } else {
            tripFile = new File(workingDir, "/Taxi_Trips_2019_07_19.csv");
        }

        File processingdir = new File(workingDir, "Scenario");
        if (processingdir.isDirectory())
            DeleteDirectory.of(processingdir, 2, 25);
        if (!processingdir.isDirectory())
            processingdir.mkdir();
        CopyFiles.now(workingDir.getAbsolutePath(), processingdir.getAbsolutePath(), //
                Arrays.asList(new String[] { "AmodeusOptions.properties", "config_full.xml", //
                        "network.xml", "network.xml.gz", "LPOptions.properties" }));
        ScenarioOptions scenarioOptions = new ScenarioOptions(processingdir, //
                ScenarioOptionsBase.getDefault());
        LocalDate simulationDate = LocalDateConvert.ofOptions(scenarioOptions.getString("date"));

        /** based on the taxi data, create a population and assemble a AMoDeus scenario */
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        System.out.println(configFile.getAbsolutePath());
        GlobalAssert.that(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        network = NetworkLoader.fromNetworkFile(new File(processingdir, configFull.network().getInputFile()));
        db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());
        FastLinkLookup fll = new FastLinkLookup(network, db);

        /** prepare for creation of scenario */
        TaxiTripsReader tripsReader = new OnlineTripsReaderChicago();
        TripBasedModifier tripModifier = new ChicagoOnlineTripBasedModifier(random, network, //
                fll, new File(processingdir, "virtualNetworkChicago"));
        TaxiTripFilter finalTripFilter = new TaxiTripFilter();
        /** trips which are faster than the network freeflow speeds would allow are removed */
        finalTripFilter.addFilter(new TripNetworkFilter(network, db, //
                Quantity.of(2.235200008, "m*s^-1"), Quantity.of(3600, "s"), Quantity.of(200, "m"), true));

        // TODO eventually remove, this did not improve the fit.
        // finalFilters.addFilter(new TripMaxSpeedFilter(network, db, ScenarioConstants.maxAllowedSpeed));
        ChicagoOnlineTripFleetConverter converter = //
                new ChicagoOnlineTripFleetConverter(scenarioOptions, network, tripModifier, //
                        new ChicagoFormatModifier(), finalTripFilter, tripsReader);
        finalTripsFile = Scenario.create(workingDir, tripFile, //
                converter, workingDir, processingdir, simulationDate, timeConvert);
        return processingdir;
    }

    private static void cleanUp(File workingDir) {
        /** delete unneeded files */
        // DeleteDirectory.of(new File(workingDir, "Scenario"), 2, 14);
        // DeleteDirectory.of(new File(workingDir, ScenarioLabels.amodeusFile), 0, 1);
        // DeleteDirectory.of(new File(workingDir, ScenarioLabels.avFile), 0, 1);
        // DeleteDirectory.of(new File(workingDir, ScenarioLabels.config), 0, 1);
        // DeleteDirectory.of(new File(workingDir, ScenarioLabels.pt2MatSettings), 0, 1);
        // DeleteDirectory.of(new File(workingDir, ScenarioLabels.network), 0, 1);
    }
}
