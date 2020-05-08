/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.io.File;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.router.FastAStarLandmarksFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.trafficmonitoring.FreeSpeedTravelTime;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;

public class TaxiData {
    private final Map<LocalDate, Integer> reqPerDay = new HashMap<>();

    public static TaxiData staticAnalyze(List<File> traceFiles, MatsimAmodeusDatabase db, //
            Network network, File saveDirectory, TaxiStampReader taxiStampReader) //
            throws Exception {
        return new TaxiData(traceFiles, db, network, saveDirectory, taxiStampReader);
    }

    private TaxiData(List<File> traceFiles, MatsimAmodeusDatabase db, Network network, //
            File saveDirectory, TaxiStampReader taxiStampReader) throws Exception {
        System.out.println("found " + traceFiles.size() + " taxi files to analyze:");

        /** read the trail files */
        Map<File, TrailFileReader> readers = new HashMap<>();
        for (int i = 0; i < traceFiles.size(); ++i) {
            System.out.println("scenario data, reading file: (" + (i + 1) + " / " + traceFiles.size() + ")");
            readers.put(traceFiles.get(i), new TrailFileReader(traceFiles.get(i), taxiStampReader));
        }

        /** collect all {@link LocalDate}s that were found */
        Set<LocalDate> localDates = readers.values().stream().map(TrailFileReader::getLocalDates).flatMap(Collection::stream).collect(Collectors.toSet());

        System.out.println("found local dates in data:");
        localDates.stream().sorted().forEach(System.out::println);

        /** analysis of the files per local date */
        LeastCostPathCalculator leastCostPathCalculator = new FastAStarLandmarksFactory(Runtime.getRuntime().availableProcessors()) //
                .createPathCalculator(network, new DistanceAsTravelDisutility(), new FreeSpeedTravelTime());
        for (LocalDate localDate : localDates) {
            int totReq = ScenarioDataHelper.processLocalDate(localDate, readers.values(), //
                    saveDirectory, new FastLinkLookup(network, db), leastCostPathCalculator, taxiStampReader.timeConvert());
            reqPerDay.put(localDate, totReq);
        }
        // code for executing total of all dates ...
        int totReq = ScenarioDataHelper.processAll(readers.values(), saveDirectory, //
                new FastLinkLookup(network, db), leastCostPathCalculator, taxiStampReader.timeConvert());
        reqPerDay.put(LocalDate.MAX, totReq);
    }
}
