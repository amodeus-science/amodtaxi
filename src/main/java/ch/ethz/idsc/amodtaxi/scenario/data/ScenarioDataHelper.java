/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.function.Function;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import org.matsim.core.router.util.LeastCostPathCalculator;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;

/* package */ enum ScenarioDataHelper {
    ;

    /* package */ static int processLocalDate(LocalDate localDate, Collection<TrailFileReader> readers, //
            File saveDirectory, FastLinkLookup fastLinkLookup, //
            LeastCostPathCalculator leastCostPathCalculator, AmodeusTimeConvert timeConvert) throws Exception {
        System.out.println("For " + localDate + ":");
        return process(readers, reader -> reader.getEntriesFor(localDate), new File(saveDirectory, localDate.toString()), fastLinkLookup, leastCostPathCalculator, timeConvert);
    }

    /* package */ static int processAll(Collection<TrailFileReader> readers, //
            File saveDirectory, FastLinkLookup fastLinkLookup, //
            LeastCostPathCalculator leastCostPathCalculator, AmodeusTimeConvert timeConvert) throws Exception {
        System.out.println("For all:");
        return process(readers, TrailFileReader::getAllEntries, new File(saveDirectory, "AllFiles"), fastLinkLookup, leastCostPathCalculator, timeConvert);
    }

    private static int process(Collection<TrailFileReader> readers, Function<TrailFileReader, NavigableMap<LocalDateTime, TaxiStamp>> getter, //
            File subDirectory, FastLinkLookup fastLinkLookup, LeastCostPathCalculator leastCostPathCalculator, AmodeusTimeConvert timeConvert) throws Exception {
        int totalRquests = 0;

        /** prepare folder to save information */
        subDirectory.mkdir();

        /** write the analyzed information */
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(subDirectory, "info"))))) {

            /** process all files for the localDate */
            Collection<FileAnalysis> filesAnalysis = new ArrayList<>();
            for (TrailFileReader reader : readers) {
                System.out.println("\tAnalyzing trail file " + reader.getFileName());
                FileAnalysis fA = new FileAnalysis( //
                        getter.apply(reader), fastLinkLookup, leastCostPathCalculator, reader.getFileName(), reader.getDateSplitUp());
                filesAnalysis.add(fA);
                totalRquests += fA.getNumRequests();
            }

            /** saving aggregate information */
            bufferedWriter.write("analyzed " + readers.size() + " files\n");
            // SaveInfo.of(filesAnalysis, bufferedWriter, subDirectory, timeConvert);
        }
        return totalRquests;
    }
}
