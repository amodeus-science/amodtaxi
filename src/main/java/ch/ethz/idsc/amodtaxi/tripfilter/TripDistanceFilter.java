/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.tripfilter;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.tensor.Scalar;

/* package */ class TripDistanceFilter extends AbstractConsciousFilter {
    private final FastLinkLookup fll;
    private final LeastCostPathCalculator lcpc;
    private final Scalar minDistance;

    public TripDistanceFilter(Network network, MatsimAmodeusDatabase db, Scalar minDistance) {
        this.minDistance = minDistance;
        // least cost path calculator
        lcpc = new FastAStarLandmarksFactory(Runtime.getRuntime().availableProcessors()).createPathCalculator(network, new DistanceAsTravelDisutility(), new FreeSpeedTravelTime());
        // fast link lookup
        fll = new FastLinkLookup(network, db);
    }

    @Override
    public boolean testInternal(TaxiTrip trip) {
        /** get origin and destination */
        Link origin = fll.linkFromWGS84(trip.pickupLoc);
        Link destin = fll.linkFromWGS84(trip.dropoffLoc);

        /** compute minimal network distance */
        // lcpc.

        // FIXME implement if needed
        throw new RuntimeException();
    }
}
