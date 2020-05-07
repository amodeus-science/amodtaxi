/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.util.Collection;
import java.util.function.Function;

/* package */ enum StaticHelper {
    ;

    public static <T extends Comparable<? super T>> T getMinVal(Collection<FileAnalysis> col, Function<FileAnalysis, T> eval) {
        return col.stream().map(eval).min(T::compareTo).orElse(null);
    }

    public static <T extends Comparable<? super T>> T getMaxVal(Collection<FileAnalysis> col, Function<FileAnalysis, T> eval) {
        return col.stream().map(eval).max(T::compareTo).orElse(null);
    }
}
