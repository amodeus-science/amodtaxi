/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.TreeSet;
import java.util.function.Function;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.scenario.FileAnalysis;
import ch.ethz.idsc.tensor.io.DeleteDirectory;

/* package */ enum StaticHelper {
    ;

    public static File prepareFolder(File workingDirectory, File outputDirectory) throws Exception {
        System.out.println("INFO working folder: " + workingDirectory.getAbsolutePath());
        System.out.println(outputDirectory.getAbsolutePath());
        System.out.println("WARN All files in the that folder will be deleted in:");
        for (int i = 2; i > 0; i--) {
            Thread.sleep(1000);
            System.err.println(i + " seconds");
        }
        if (workingDirectory.exists()) {
            if (outputDirectory.exists())
                DeleteDirectory.of(outputDirectory, 5, 200000);
            outputDirectory.mkdir();
        }
        return outputDirectory;
    }

    public static <T> T getMinVal(Collection<FileAnalysis> col, Function<FileAnalysis, T> eval) {
        try {
            return sortedVals(col, eval).first();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    public static <T> T getMaxVal(Collection<FileAnalysis> col, Function<FileAnalysis, T> eval) {
        try {
            return sortedVals(col, eval).last();
        } catch (NoSuchElementException ex) {
            return null;
        }
    }

    private static <T> TreeSet<T> sortedVals(Collection<FileAnalysis> col, Function<FileAnalysis, T> eval) {
        GlobalAssert.that(col.size() > 0);
        TreeSet<T> vals = new TreeSet<>();
        for (FileAnalysis fA : col) {
            T t = eval.apply(fA);
            if (Objects.nonNull(t)) {
                vals.add(t);
            }
        }
        return vals;
    }

}
