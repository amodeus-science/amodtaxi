/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.SortedMap;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class NetworkDistanceHelper {
    private Scalar custrDistance = Quantity.of(0, SI.METER);
    private Scalar totalDistance = Quantity.of(0, SI.METER);
    private Scalar emptyDistance = Quantity.of(0, SI.METER);

    public NetworkDistanceHelper(SortedMap<LocalDateTime, TaxiStamp> sortedEntries, //
            FastLinkLookup fastLinkLookup, LeastCostPathCalculator leastCostPathCalculator) {
        /** compute */
        Link linkStart = fastLinkLookup.linkFromWGS84(sortedEntries.get(sortedEntries.firstKey()).gps);
        Link linkPrev = linkStart;
        boolean occPrev = sortedEntries.get(sortedEntries.firstKey()).occupied;

        for (LocalDateTime time : sortedEntries.keySet()) {
            boolean occ = sortedEntries.get(time).occupied;

            // 1: lat 0: lng
            Link currLink = fastLinkLookup.linkFromWGS84(sortedEntries.get(time).gps);
            if (occ != occPrev) {
                Scalar distance = distance(linkStart, linkPrev, leastCostPathCalculator);
                totalDistance = totalDistance.add(distance);
                if (occ) // request started
                    emptyDistance = emptyDistance.add(distance);
                else // request ended
                    custrDistance = custrDistance.add(distance);
                linkStart = currLink;
            }
            linkPrev = currLink;
            occPrev = occ;
        }
    }

    private static Scalar distance(Link fromLink, Link toLink, LeastCostPathCalculator leastCostPathCalculator) {
        Path shortest = leastCostPathCalculator.calcLeastCostPath(fromLink.getFromNode(), toLink.getToNode(), 1, null, null);
        return Quantity.of(shortest.links.stream().mapToDouble(Link::getLength).sum(), SI.METER);
    }

    public Scalar getEmptyDistance() {
        return emptyDistance;
    }

    public Scalar getCustrDistance() {
        return Objects.requireNonNull(custrDistance);
    }

    public Scalar getTotlDistance() {
        return totalDistance;
    }
}
