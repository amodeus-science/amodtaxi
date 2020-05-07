/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.TaxiStampConvertedSF;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.amodtaxi.util.CSVUtils;
import ch.ethz.idsc.amodtaxi.util.ReverseLineInputStream;
import ch.ethz.idsc.tensor.Tensor;

/* package */ class TrailFileReader {
    private final NavigableMap<LocalDateTime, TaxiStamp> sortedStamps = new TreeMap<>();
    private Set<LocalDate> localDates = new HashSet<>();
    private final String fileName;
    private HashMap<LocalDate, Tensor> dateSplitUp = new HashMap<>();
    private final AmodeusTimeConvert timeConvert;

    public TrailFileReader(File trailFile, AmodeusTimeConvert timeConvert) throws Exception {
        this.timeConvert = timeConvert;
        read(trailFile);
        fileName = trailFile.getName();
    }

    private void read(File trailFile) throws Exception {
        /** read file */
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new ReverseLineInputStream(trailFile)))) {
            bufferedReader.lines().forEach(line -> {
                List<String> csvRow = CSVUtils.csvLineToList(line, " ");
                TaxiStamp stamp = TaxiStampConvertedSF.INSTANCE.from(csvRow, timeConvert);

                // int time = Integer.parseInt(csvRow.get(3));
                sortedStamps.put(stamp.globalTime, stamp);
                localDates.add(timeConvert.toLocalDate(csvRow.get(3)));
            });
        }
    }

    public NavigableMap<LocalDateTime, TaxiStamp> getAllEntries() {
        return sortedStamps;
    }

    public SortedMap<LocalDateTime, TaxiStamp> getEntriesFor(LocalDate localDate) {
        return sortedStamps.entrySet().stream() //
                .filter(e -> e.getKey().toLocalDate().equals(localDate)) //
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2, TreeMap::new));
    }

    /** @param minTime belonging to certain LocalDate
     * @return first key in LocalDate for which the occupancy status is zero, traces from the previous day
     *         should be kept at the previous day */
    private LocalDateTime beginEntry(LocalDateTime minTime, LocalDate localDate) {
        LocalDateTime startKey = sortedStamps.floorKey(minTime);
        if (Objects.isNull(startKey)) {
            LocalDateTime upper = sortedStamps.ceilingKey(minTime);
            if (upper.isBefore(timeConvert.endOf(localDate)))
                startKey = upper;
            else
                return null;
        }

        boolean occ = sortedStamps.get(startKey).occupied;
        while (occ && startKey.isBefore(sortedStamps.lastKey())) {
            startKey = sortedStamps.higherKey(startKey);
            occ = sortedStamps.get(startKey).occupied;// Integer.parseInt(sortedEntries.get(current).get(2));
        }
        return startKey;
    }

    /** @param maxTime belonging to certain LocalDate
     * @return if occupancy status is zero, return upper entry, maxTime, else return first 0 timestep after current 1 series status is 1 in the current
     *         sequence. */
    private LocalDateTime endEntry(LocalDateTime maxTime) {
        LocalDateTime current = sortedStamps.floorKey(maxTime);
        if (Objects.isNull(current))
            return null;
        boolean occ = sortedStamps.get(current).occupied;
        while (occ && current.isBefore(sortedStamps.lastKey())) {
            current = sortedStamps.higherKey(current);
            if (Objects.isNull(current))
                throw new RuntimeException();
            occ = sortedStamps.get(current).occupied;// Integer.parseInt(sortedEntries.get(current).get(2));
        }
        return current;
    }

    public Set<LocalDate> getLocalDates() {
        return localDates;
    }

    public void printLocalDates() {
        localDates.stream().sorted().forEach(System.out::println);
    }

    public String getFileName() {
        return fileName;
    }

    public HashMap<LocalDate, Tensor> getDateSplitUp() {
        return dateSplitUp;
    }
}
