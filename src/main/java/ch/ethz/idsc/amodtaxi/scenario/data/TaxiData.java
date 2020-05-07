/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.io.File;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.geo.FastQuadTree;

public class TaxiData {
    private final Map<LocalDate, Integer> reqPerDay = new HashMap<>();

    public static TaxiData staticAnalyze(List<File> traceFiles, MatsimAmodeusDatabase db, //
            Network network, File saveDirectory, AmodeusTimeConvert timeConvert) //
            throws Exception {
        return new TaxiData(traceFiles, db, network, saveDirectory, timeConvert);
    }

    private TaxiData(List<File> traceFiles, MatsimAmodeusDatabase db, Network network, //
            File saveDirectory, AmodeusTimeConvert timeConvert) throws Exception {
        System.out.println("found " + traceFiles.size() + " taxi files to analyze:");

        /** read the trail files */
        Map<File, TrailFileReader> readers = new HashMap<>();
        for (int i = 0; i < traceFiles.size(); ++i) {
            System.out.println("scenario data, reading file: (" + (i + 1) + " / " + traceFiles.size() + ")");
            readers.put(traceFiles.get(i), new TrailFileReader(traceFiles.get(i), timeConvert));
        }

        /** collect all {@link LocalDate}s that were found */
        Set<LocalDate> localDates = new HashSet<>();
        localDates.add(LocalDate.MAX); // code for executing total of all dates ...
        readers.values().forEach(tfr -> localDates.addAll(tfr.getLocalDates()));

        System.out.println("found local dates in data:");
        localDates.stream().sorted().filter(ld -> !ld.equals(LocalDate.MAX)).forEach(System.out::println);

        /** analysis of the files per local date */
        LeastCostPathCalculator leastCostPathCalculator = new FastAStarLandmarksFactory(Runtime.getRuntime().availableProcessors()) //
                .createPathCalculator(network, new DistanceAsTravelDisutility(), new FreeSpeedTravelTime());
        QuadTree<Link> quadTree = FastQuadTree.of(network);
        for (LocalDate localDate : localDates) {
            Integer totReq = ScenarioDataHelper.processLocalDate(localDate, readers.values(), //
                    saveDirectory, new FastLinkLookup(network, db), leastCostPathCalculator, timeConvert);
            reqPerDay.put(localDate, totReq);
        }
    }
}
