/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.util.Collection;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import org.matsim.api.core.v01.Coord;

@Deprecated /** use {@link ch.ethz.idsc.amodtaxi.trace.TaxiStampHelpers} instead */
/* package */ enum LongLatRange {
    ;

    /** @param taxiStamps {@link Collection} of {@link TaxiStamp}
     * @return {minLat, maxLat, minLong, maxLong} */
    public static Tensor in(Collection<TaxiStamp> taxiStamps) {
        Collection<Coord> coords = taxiStamps.stream().map(taxiStamp -> taxiStamp.gps).collect(Collectors.toList());
        GlobalAssert.that(!coords.isEmpty());
        double minLat = coords.stream().mapToDouble(Coord::getY).min().getAsDouble();
        double maxLat = coords.stream().mapToDouble(Coord::getY).max().getAsDouble();
        double minLng = coords.stream().mapToDouble(Coord::getX).min().getAsDouble();
        double maxLng = coords.stream().mapToDouble(Coord::getX).max().getAsDouble();
        return Tensors.vector(minLat, maxLat, minLng, maxLng);
    }
}
