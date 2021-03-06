/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package amodeus.amodtaxi.linkspeed.iterative;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

import amodeus.amodeus.linkspeed.LinkSpeedDataContainer;
import amodeus.amodeus.net.MatsimAmodeusDatabase;
import amodeus.amodeus.taxitrip.ShortestDurationCalculator;
import amodeus.amodeus.taxitrip.TaxiTrip;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.util.LeastCostPathCalculator;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

/* package */ class FindCongestionIterative {
    private final TripComparisonMaintainer tripMaintainer;
    private final RandomTripMaintainer randomTrips;
    private final File processingDir;
    private Scalar lastCost;

    /** settings and data */
    private final Scalar tolerance;
    private final Network network;
    private final LinkSpeedDataContainer lsData;
    private final MatsimAmodeusDatabase db;
    /** this is a value in (0,1] which determines the convergence
     * speed of the algorithm, a value close to 1 may lead to
     * loss of convergence, it is advised to chose slow. No changes
     * are applied for epsilon == 0. */
    private final Scalar epsilon1;
    /** probability of taking a new trip */
    private final Scalar epsilon2;
    private final int maxIter;
    private final Random random;
    private final int dt;

    public FindCongestionIterative(Network network, MatsimAmodeusDatabase db, File processingDir, //
            LinkSpeedDataContainer lsData, List<TaxiTrip> allTrips, //
            int maxIter, Scalar tol, Scalar epsilon1, Scalar epsilon2, Random random, int dt, //
            Function<List<Scalar>, Scalar> costFunction, int checkHorizon) {
        this.processingDir = processingDir;
        this.network = network;
        this.db = db;
        this.tolerance = Objects.requireNonNull(tol);
        this.lsData = lsData;
        this.epsilon1 = epsilon1;
        this.epsilon2 = epsilon2;
        this.maxIter = maxIter;
        this.random = random;
        this.dt = dt;

        /** export the initial distribution of ratios */
        this.randomTrips = new RandomTripMaintainer(allTrips, checkHorizon, costFunction, random);
        this.tripMaintainer = new TripComparisonMaintainer(randomTrips, network, db);

        /** export initial distribution */
        File diff = new File(processingDir, "diff");
        File plot = new File(processingDir, "plot");
        diff.mkdir();
        plot.mkdir();
        StaticHelper.exportRatioMap(diff, tripMaintainer.getLookupMap(), "Initial");
        StaticHelper.plotRatioMap(plot, randomTrips.getRatios(), "Initial");

        /** show initial score */
        System.out.println("Cost initial: " + randomTrips.getRatioCost());

        runTripIterations();

        System.out.println("Cost End: " + randomTrips.getRatioCost());
    }

    private void runTripIterations() {
        int iterationCount = 0;
        lastCost = randomTrips.getRatioCost();
        System.out.println("Last cost before start: " + lastCost);
        System.out.println("Tolerance:              " + tolerance);

        while (Scalars.lessEquals(tolerance, lastCost) && iterationCount < maxIter) {
            ++iterationCount;

            /** taking random trip */
            boolean isRandomTrip = random.nextDouble() <= epsilon2.number().doubleValue();
            TaxiTrip trip = isRandomTrip //
                    ? randomTrips.nextRandom() //
                    : tripMaintainer.getWorst(); // take currently worst trip

            /** create the shortest duration calculator using the linkSpeed data,
             * must be done again to take into account newest updates */
            DurationCompare compare = getPathDurationRatio(trip);
            Scalar pathDurationratio = compare.nwPathDurationRatio;
            Scalar ratioBefore = compare.nwPathDurationRatio;

            /** if it is a random trip, record the ratio */
            if (isRandomTrip)
                randomTrips.addRecordedRatio(ratioBefore);

            /** update cost based on random trips */
            lastCost = randomTrips.getRatioCost();

            /** rescale factor such that epsilon in [0,1] maps to [f,1] */
            Scalar rescaleFactor = RealScalar.ONE.subtract( //
                    (RealScalar.ONE.subtract(pathDurationratio)).multiply(epsilon1));

            /** rescale links to approach desired link speed */
            ApplyScaling.to(lsData, trip, compare.path, rescaleFactor, dt);

            compare = getPathDurationRatio(trip);
            pathDurationratio = compare.nwPathDurationRatio;

            if (!StaticHelper.ratioDidImprove(ratioBefore, pathDurationratio)) {
                // if(true){
                System.err.println("trip:        " + trip.localId);
                System.err.println("ratioBefore: " + ratioBefore);
                System.err.println("ratioAfter:  " + pathDurationratio);
                System.err.println("Now at trip " + trip.localId);
                System.err.println("Now at trip " + trip.pickupLoc);
                System.err.println("Now at trip " + trip.dropoffLoc);
                System.err.println("compare.path");
                compare.path.links.forEach(l -> System.err.println(l.getId().toString()));
                System.err.println("compare.pathDist " + compare.pathDist);
                System.err.println("compare.duration " + compare.duration);
                System.err.println("compare.pathTime " + compare.pathTime);
            }

            tripMaintainer.update(trip, pathDurationratio);

            /** assess every 20 trips if ok */
            if (iterationCount % 10 == 0) {
                // costMid = costFunction.apply(tripMaintainer.getLookupMap());
                System.out.println("iterationCount:       " + iterationCount);
                // System.out.println("cost: " + costMid);
                System.out.println("worst cost: " + tripMaintainer.getWorstCost());
                System.out.println("worst trip: " + tripMaintainer.getWorst().localId);
                System.out.println("cost:       " + lastCost);
                // if (Scalars.lessEquals(costMid, tolerance))
                // break;
            }

            // DEBUGGING
            /** DEBUGGING every interval trips, export cost map */
            if (iterationCount % 50 == 0) {
                StaticHelper.exportRatioMap(new File(processingDir, "diff"), tripMaintainer.getLookupMap(), Integer.toString(iterationCount));
                StaticHelper.plotRatioMap(new File(processingDir, "plot"), randomTrips.getRatios(), Integer.toString(iterationCount));
            }
            // DEBUGGING END

            /** intermediate export */
            if (iterationCount % 30000 == 0)
                StaticHelper.export(processingDir, lsData, "_" + Integer.toString(iterationCount));
        }
        System.out.println("---- " + iterationCount + " ----");
    }

    private DurationCompare getPathDurationRatio(TaxiTrip trip) {
        /** create the shortest duration calculator using the linkSpeed data,
         * must be done again to take into account newest updates */
        LeastCostPathCalculator lcpc = LinkSpeedLeastPathCalculator.from(network, lsData);
        ShortestDurationCalculator calc = new ShortestDurationCalculator(lcpc, network, db);

        /** compute ratio of network path and trip duration f */
        return new DurationCompare(trip, calc);
    }
}
