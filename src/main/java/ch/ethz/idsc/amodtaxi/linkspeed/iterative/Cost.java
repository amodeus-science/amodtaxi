/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.linkspeed.iterative;

import java.util.List;
import java.util.Map;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.red.Mean;

/** Cost must be positive */
/* package */ enum Cost {
    ;
    // calculates mean of error ratios(real vs simulation) of all taxitrips in ratioMap
    public static Scalar mean(Map<TaxiTrip, Scalar> ratioMap) {
        Tensor diffAll = Tensors.empty();
        ratioMap.values().forEach(s -> diffAll.append(s));
        Scalar cost = ((Scalar) Mean.of(diffAll)).subtract(RealScalar.ONE).abs();
        // removed assert, because abs() call
        return cost;
    }

    public static Scalar max(List<Scalar> ratios) {
        Scalar cost = ratios.stream()//
                .map(s -> s.subtract(RealScalar.ONE).abs()).reduce(Max::of).get();
        // removed assert because of abs() call
        return cost;
    }

}
