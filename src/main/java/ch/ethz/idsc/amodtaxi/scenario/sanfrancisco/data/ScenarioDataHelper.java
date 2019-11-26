/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.data;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodtaxi.scenario.FileAnalysis;

// TODO is this generic? if yes, move to super package
/* package */ enum ScenarioDataHelper {
    ;

    /* package */ static Integer processLocalDate(LocalDate localDate, Collection<TrailFileReader> readers, //
            File saveDirectory, MatsimAmodeusDatabase db, Network network, //
            LeastCostPathCalculator leastCostPathCalculator, QuadTree<Link> quadTree, //
            AmodeusTimeConvert timeConvert) throws Exception {

        Integer totalRquests = 0;

        /** prepare folder to save information */
        File saveSubDir = localDate.equals(LocalDate.MAX) //
                ? new File(saveDirectory, "AllFiles") : new File(saveDirectory, localDate.toString());
        saveSubDir.mkdir();

        /** write the analyzed information */
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(saveSubDir, "info"))))) {

            /** process all files for the localDate */
            Collection<FileAnalysis> filesAnalysis = new ArrayList<>();
            for (TrailFileReader reader : readers) {
                System.out.println("Analyzing trail file " + reader.getFileName());
                FileAnalysis fA = new FileAnalysis(reader.getEntriesFor(localDate), //
                        db, network, leastCostPathCalculator, quadTree, reader.getFileName(), reader.getDateSplitUp());
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
