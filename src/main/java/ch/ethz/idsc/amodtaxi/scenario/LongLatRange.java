/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.util.SortedMap;
import java.util.TreeSet;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ enum LongLatRange {
    ;

    /** @param sortedEntries
     * @return {minLat, maxLat, minLong, maxLong} */
    public static <T> Tensor in(SortedMap<T, TaxiStamp> sortedEntries) {
        GlobalAssert.that(sortedEntries.size() > 0);
        TreeSet<Double> sortedLat = new TreeSet<>();
        TreeSet<Double> sortedLng = new TreeSet<>();
        for (TaxiStamp stamp : sortedEntries.values()) {
            sortedLng.add(stamp.gps.getX());
            sortedLat.add(stamp.gps.getY());
        }
        return Tensors.vector(sortedLat.first(), sortedLat.last(), sortedLng.first(), sortedLng.last());
    }
}
