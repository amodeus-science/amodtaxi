/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import java.util.Objects;

import ch.ethz.idsc.tensor.Tensor;

/* package */ enum StaticHelper {
    ;

    public static String addLocationIfFound(Tensor coordsStart, Tensor coordsEnd) {
        if (Objects.nonNull(coordsStart) && Objects.nonNull(coordsEnd))
            return ("," + coordsStart.toString().replace(",", ";") + "," + coordsEnd.toString().replace(",", ";"));

        if (Objects.isNull(coordsStart) && Objects.nonNull(coordsEnd))
            return ("," + "undef" + "," + coordsEnd.toString().replace(",", ";"));

        if (Objects.nonNull(coordsStart) && Objects.isNull(coordsEnd))
            return ("," + coordsStart.toString().replace(",", ";") + "," + "undef");

        // both are null
        return ("," + "undef" + "," + "undef");

    }

}
