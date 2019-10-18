/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

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

/* package */ enum ScenarioDataHelper {
    ;

    /* package */ static Integer processLocalDate(LocalDate localDate, Collection<TrailFileReader> readers, //
            File saveDirectory, MatsimAmodeusDatabase db, Network network, LeastCostPathCalculator lcpc, QuadTree<Link> qt, //
            AmodeusTimeConvert timeConvert) throws Exception {

        Integer totalRquests = 0;

        /** prepare folder to save information */
        File saveSubDir = null;
        if (localDate.equals(LocalDate.MAX))
            saveSubDir = new File(saveDirectory, (("AllFiles")));
        else
            saveSubDir = new File(saveDirectory, ((localDate.toString())));
        saveSubDir.mkdir();

        /** write the analyzed information */
        try (BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(saveSubDir + "/info"))))) {

            /** process all files for the localDate */
            Collection<FileAnalysis> filesAnalysis = new ArrayList<>();
            for (TrailFileReader reader : readers) {
                System.out.println("Analyzing trail file " + reader.getFileName());
                FileAnalysis fA = new FileAnalysis(reader.getEntriesFor(localDate), //
                        db, network, lcpc, qt, reader.getFileName(), reader.getDateSplitUp());
                filesAnalysis.add(fA);
                totalRquests += fA.getNumRequests();
            }

            /** saving aggregate information */
            out.write("analzyed " + readers.size() + " files\n");
            SaveInfo.of(filesAnalysis, out, saveSubDir, timeConvert);
        }
        return totalRquests;
    }
}
