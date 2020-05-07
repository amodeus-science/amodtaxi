/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import org.matsim.core.router.util.LeastCostPathCalculator;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;

/* package */ enum ScenarioDataHelper {
    ;

    /* package */ static Integer processLocalDate(LocalDate localDate, Collection<TrailFileReader> readers, //
            File saveDirectory, FastLinkLookup fastLinkLookup, //
            LeastCostPathCalculator leastCostPathCalculator, AmodeusTimeConvert timeConvert) throws Exception {
        int totalRquests = 0;

        /** prepare folder to save information */
        File saveSubDir = new File(saveDirectory, localDate.equals(LocalDate.MAX) ? "AllFiles" : localDate.toString());
        saveSubDir.mkdir();

        /** write the analyzed information */
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(saveSubDir, "info"))))) {

            /** process all files for the localDate */
            Collection<FileAnalysis> filesAnalysis = new ArrayList<>();
            for (TrailFileReader reader : readers) {
                System.out.println("Analyzing trail file " + reader.getFileName());
                FileAnalysis fA = new FileAnalysis( //
                        reader.getEntriesFor(localDate), fastLinkLookup, leastCostPathCalculator, reader.getFileName(), reader.getDateSplitUp());
                filesAnalysis.add(fA);
                totalRquests += fA.getNumRequests();
            }

            /** saving aggregate information */
            bufferedWriter.write("analzyed " + readers.size() + " files\n");
            // SaveInfo.of(filesAnalysis, bufferedWriter, saveSubDir, timeConvert);
        }
        return totalRquests;
    }
}
