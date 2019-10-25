/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.SortedMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.geo.ClosestLinkSelect;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class NetworkDistanceHelper {

    private Scalar custrDistance = Quantity.of(0, SI.METER);
    private Scalar totalDistance = Quantity.of(0, SI.METER);
    private Scalar emptyDistance = Quantity.of(0, SI.METER);

    public NetworkDistanceHelper(SortedMap<LocalDateTime, TaxiStamp> sortedEntries, //
            MatsimAmodeusDatabase db, LeastCostPathCalculator lcpc, QuadTree<Link> qt) {

        /** initialize */
        ClosestLinkSelect linkSelect = new ClosestLinkSelect(db, qt);

        /** compute */
        Link linkStart = linkSelect.linkFromWGS84(sortedEntries.get(sortedEntries.firstKey()).gps);
        Link linkPrev = linkStart;
        boolean occPrev = sortedEntries.get(sortedEntries.firstKey()).occupied;

        for (LocalDateTime time : sortedEntries.keySet()) {
            boolean occ = sortedEntries.get(time).occupied;

            // 1: lat 0: lng
            Link currLink = linkSelect.linkFromWGS84(sortedEntries.get(time).gps);
            if (!occPrev && occ) { // request started

                Path shortest = lcpc.calcLeastCostPath(linkStart.getFromNode(), linkPrev.getToNode(), 1, null, null);
                double distance = 0.0;
                for (Link link : shortest.links)
                    distance += link.getLength();

                totalDistance = totalDistance.add(Quantity.of(distance, SI.METER));
                emptyDistance = emptyDistance.add(Quantity.of(distance, SI.METER));

                linkStart = currLink;
            }
            if (occPrev && !occ) { // request ended

                Path shortest = lcpc.calcLeastCostPath(linkStart.getFromNode(), linkPrev.getToNode(), 1, null, null);
                double distance = 0.0;
                for (Link link : shortest.links)
                    distance += link.getLength();

                totalDistance = totalDistance.add(Quantity.of(distance, SI.METER));
                custrDistance = custrDistance.add(Quantity.of(distance, SI.METER));

                linkStart = currLink;
            }
            linkPrev = currLink;
            occPrev = occ;
        }
    }

    public Scalar getEmptyDistance() {
        return emptyDistance;
    }

    public Scalar getCustrDistance() {
        Objects.requireNonNull(custrDistance);
        return custrDistance;
    }

    public Scalar getTotlDistance() {
        return totalDistance;
    }
}
