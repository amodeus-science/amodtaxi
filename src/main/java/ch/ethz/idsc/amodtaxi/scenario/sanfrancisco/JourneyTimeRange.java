/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.util.TreeSet;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ enum JourneyTimeRange {
    ;

    /** @param journeyTimes
     * @return {minJT, maxJT} */
    public static Tensor in(Tensor journeyTimes) {
        if (journeyTimes.length() == 0)
            return null;
        TreeSet<Integer> vals = new TreeSet<>();
        journeyTimes.flatten(-1).forEach(s -> vals.add(((Scalar) s).number().intValue()));
        return Tensors.vector(vals.first(), vals.last());
    }
}
