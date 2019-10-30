/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.linkspeed.iterative;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.taxitrip.ImportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.linkspeed.TaxiLinkSpeedEstimator;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.io.HomeDirectory;

public class IterativeLinkSpeedEstimator implements TaxiLinkSpeedEstimator {

    private LinkSpeedDataContainer lsData;

    private final int maxIter;
    private final Scalar tolerance = RealScalar.of(0.005);
    /** this is a value in (0,1] which determines the convergence
     * speed of the algorithm, a value close to 1 may lead to
     * loss of convergence, it is advised o chose slow values for
     * epsilon. No changes are applied for epsilon == 0. */
    private final Scalar epsilon1 = RealScalar.of(0.05);
    /** probability of taking a new trip */
    private final Scalar epsilon2 = RealScalar.of(0.8);
    private final Random random = new Random(123);
    private final int dt = 450;

    public IterativeLinkSpeedEstimator(int maxIter) {
        this.maxIter = maxIter;
    }

    public void compute(File processingDir, Network network, MatsimAmodeusDatabase db, List<TaxiTrip> trips) {

        // // network and database
        // ScenarioOptions scenarioOptions = new ScenarioOptions(processingDir, //
        // ScenarioOptionsBase.getDefault());
        // File configFile = new File(scenarioOptions.getPreparerConfigName());
        // System.out.println(configFile.getAbsolutePath());
        // GlobalAssert.that(configFile.exists());
        // Config configFull = ConfigUtils.loadConfig(configFile.toString());
        // Network network = NetworkLoader.fromNetworkFile(new File(processingDir, configFull.network().getInputFile()));
        // MatsimAmodeusDatabase db = //
        // MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());

        /** create link speed data container */
        lsData = new LinkSpeedDataContainer();

        /** load initial trips */

        System.out.println("Number of trips: " + trips.size());

        new FindCongestionIterative(network, db, processingDir, lsData, trips, maxIter, //
                tolerance, epsilon1, epsilon2, random, dt, m -> Cost.max(m), trips.size());

        /** final export */
        StaticHelper.export(processingDir, lsData, "");

    }

    @Override
    public LinkSpeedDataContainer getLsData() {
        return Objects.requireNonNull(lsData);
    }

    // -------

    public static void main(String[] args) throws IOException {

        File processingDir = HomeDirectory.file("data/TaxiComparison_ChicagoScCr/Scenario");
        File finalTripsFile = HomeDirectory.file("data/TaxiComparison_ChicagoScCr/Scenario/"//
                + "tripData/Taxi_Trips_2019_07_19_prepared_filtered_modified_final.csv");

        /** creating finding network and Matsimamodeusdatabase */
        // network and database
        ScenarioOptions scenarioOptions = new ScenarioOptions(processingDir, //
                ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        System.out.println(configFile.getAbsolutePath());
        GlobalAssert.that(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        Network network = NetworkLoader.fromNetworkFile(new File(processingDir, configFull.network().getInputFile()));
        MatsimAmodeusDatabase db = //
                MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());

        /** generating the trips file */
        List<TaxiTrip> trips = new ArrayList<>();
        ImportTaxiTrips.fromFile(finalTripsFile).//
                forEach(tt -> trips.add(tt));
        new IterativeLinkSpeedEstimator(200000).compute(processingDir, network, db, trips);
    }

}
