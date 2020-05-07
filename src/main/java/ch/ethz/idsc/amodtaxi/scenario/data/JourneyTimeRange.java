/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.red.Min;

/* package */ enum JourneyTimeRange {
    ;

    /** @param journeyTimes {@link Tensor}
     * @return {minJT, maxJT} */
    public static Tensor in(Tensor journeyTimes) {
        return Tensors.of(min(journeyTimes), max(journeyTimes));
    }

    /** @param journeyTimes {@link Tensor}
     * @return minJT */
    public static Scalar min(Tensor journeyTimes) {
        return journeyTimes.flatten(-1).map(Scalar.class::cast).reduce(Min::of).orElseThrow();
    }

    /** @param journeyTimes {@link Tensor}
     * @return maxJT */
    public static Scalar max(Tensor journeyTimes) {
        return journeyTimes.flatten(-1).map(Scalar.class::cast).reduce(Max::of).orElseThrow();
    }
}
