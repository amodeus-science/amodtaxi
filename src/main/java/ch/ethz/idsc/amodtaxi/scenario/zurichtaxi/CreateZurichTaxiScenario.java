/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.matsim.api.core.v01.network.Network;
import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.taxitrip.ImportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodtaxi.linkspeed.iterative.IterativeLinkSpeedEstimator;
import ch.ethz.idsc.amodtaxi.osm.OsmLoader;
import ch.ethz.idsc.amodtaxi.scenario.FinishedScenario;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioBasicNetworkPreparer;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;

public class CreateZurichTaxiScenario {

    private final File workingDir;
    private final File processingDir;
    private File finalTripsFile;
    private Network network = null;
    private MatsimAmodeusDatabase db = null;
    private final int maxIter = 100000;

    public CreateZurichTaxiScenario(File workingDir) throws Exception {
        this.workingDir = workingDir;
        ZurichSetup.in(workingDir);
        processingDir = run();
        File destinDir = new File(workingDir, "CreatedScenario");
        Objects.requireNonNull(finalTripsFile);

        System.out.println("The final trips file is: ");
        System.out.println(finalTripsFile.getAbsolutePath());

        /** loading final trips */
        List<TaxiTrip> trips = new ArrayList<>();
        ImportTaxiTrips.fromFile(finalTripsFile).//
                forEach(tt -> trips.add(tt));
        new IterativeLinkSpeedEstimator(maxIter).compute(processingDir, network, db, trips);
        FinishedScenario.copyToDir(workingDir.getAbsolutePath(), //
                processingDir.getAbsolutePath(), //
                destinDir.getAbsolutePath());
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


        /** load taxi data from the trips file */
        
        
        

        //
        // File processingdir = new File(workingDir, "Scenario");
        // if (processingdir.isDirectory())
        // DeleteDirectory.of(processingdir, 2, 25);
        // if (!processingdir.isDirectory())
        // processingdir.mkdir();
        // CopyFiles.now(workingDir.getAbsolutePath(), processingdir.getAbsolutePath(), //
        // Arrays.asList(new String[] { "AmodeusOptions.properties", "config_full.xml", //
        // "network.xml", "network.xml.gz", "LPOptions.properties" }));
        // ScenarioOptions scenarioOptions = new ScenarioOptions(processingdir, //
        // ScenarioOptionsBase.getDefault());
        // LocalDate simulationDate = LocalDateConvert.ofOptions(scenarioOptions.getString("date"));
        //
        // //
        // File configFile = new File(scenarioOptions.getPreparerConfigName());
        // System.out.println(configFile.getAbsolutePath());
        // GlobalAssert.that(configFile.exists());
        // Config configFull = ConfigUtils.loadConfig(configFile.toString());
        // network = NetworkLoader.fromNetworkFile(new File(processingdir, configFull.network().getInputFile()));
        // db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());
        // FastLinkLookup fll = new FastLinkLookup(network, db);
        //
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
        // finalTripsFile = Scenario.create(workingDir, tripFile, //
        // converter, workingDir, processingdir, simulationDate, timeConvert);
        // return processingdir;

        return null;
    }

    // --
    public static void main(String[] args) throws Exception {
        File workingDir = new File(args[0]);
        new CreateZurichTaxiScenario(workingDir);
    }

}
