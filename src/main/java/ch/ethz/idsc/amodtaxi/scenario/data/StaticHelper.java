/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/* package */ enum StaticHelper {
    ;

    public static <T extends Comparable<? super T>> Optional<T> getMinVal(Collection<FileAnalysis> col, Function<FileAnalysis, Optional<T>> eval) {
        return col.stream().map(eval).filter(Optional::isPresent).map(Optional::get).min(T::compareTo);
    }

    public static <T extends Comparable<? super T>> Optional<T> getMaxVal(Collection<FileAnalysis> col, Function<FileAnalysis, Optional<T>> eval) {
        return col.stream().map(eval).filter(Optional::isPresent).map(Optional::get).max(T::compareTo);
    }
}
