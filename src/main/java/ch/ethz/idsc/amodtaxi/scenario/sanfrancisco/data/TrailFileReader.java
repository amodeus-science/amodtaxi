package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.LocalDateTimes;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.ReverseLineInputStream;
import ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.TaxiStampConvertedSF;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.amodtaxi.util.CSVUtils;
import ch.ethz.idsc.tensor.Tensor;

/* package */ class TrailFileReader {

    private final TreeMap<LocalDateTime, TaxiStamp> sortedStamps = new TreeMap<>();
    private HashSet<LocalDate> localDates = new HashSet<>();
    private final String fileName;
    private LocalDateTime beginKey;
    private LocalDateTime endKey;
    private HashMap<LocalDate, Tensor> dateSplitUp = new HashMap<>();
    private final AmodeusTimeConvert timeConvert;

    public TrailFileReader(File trailFile, AmodeusTimeConvert timeConvert) throws Exception {
        this.timeConvert = timeConvert;
        read(trailFile);
        fileName = trailFile.getName();
    }

    private void read(File trailFile) throws Exception {

        /** read file */
        // FileInputStream fis = new FileInputStream(trailFile);
        try (BufferedReader in = new BufferedReader(new InputStreamReader(new ReverseLineInputStream(trailFile)))) {

            String line = null;
            while ((line = in.readLine()) != null) {
                List<String> csvRow = CSVUtils.csvLineToList(line, " ");
                TaxiStamp stamp = TaxiStampConvertedSF.INSTANCE.from(csvRow, timeConvert);

                // int time = Integer.parseInt(csvRow.get(3));
                sortedStamps.put(stamp.globalTime, stamp);
                localDates.add(timeConvert.toLocalDate(csvRow.get(3)));
            }

        }
        // fis.close();
    }

    public TreeMap<LocalDateTime, TaxiStamp> getAllEntries() {
        return sortedStamps;
    }

    public SortedMap<LocalDateTime, TaxiStamp> getEntriesFor(LocalDate localDate) {
        SortedMap<LocalDateTime, TaxiStamp> map = new TreeMap<>();
        sortedStamps.entrySet().stream()//
                .filter(e -> e.getKey().toLocalDate().equals(localDate))//
                .forEach(e -> map.put(e.getKey(), e.getValue()));
        return map;

        // return (SortedMap<LocalDateTime, TaxiStamp>) sortedStamps.entrySet().stream()//
        // .filter(e -> e.getKey().toLocalDate().equals(localDate))//
        // .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        // old implementation, TODO delete
        // if (localDate.equals(LocalDate.MAX))
        // return getAllEntries();
        // LocalDateTime minTime = timeConvert.beginOf(localDate);
        // LocalDateTime maxTime = timeConvert.endOf(localDate);
        //
        // beginKey = beginEntry(minTime, localDate);
        // endKey = endEntry(maxTime);
        //
        // if (Objects.nonNull(beginKey) && Objects.nonNull(endKey)) {
        // dateSplitUp.put(localDate, Tensors.vector(beginKey, endKey));
        // } else {
        // Tensor append = Tensors.vector(-1, -1);
        // if (Objects.nonNull(beginKey))
        // append.set(RealScalar.of(beginKey), 0);
        //
        // if (Objects.nonNull(beginKey))
        // append.set(RealScalar.of(endKey), 1);
        //
        // dateSplitUp.put(localDate, append);
        // }
        //
        // if (Objects.isNull(beginKey) || Objects.isNull(endKey))
        // return null;
        // return sortedStamps.subMap(beginKey, endKey);
    }

    /** @param minTime belonging to certain LocalDate
     * @return first key in LocalDate for which the occupancy status is zero, traces from the previous day
     *         should be kept at the previous day */
    private LocalDateTime beginEntry(LocalDateTime minTime, LocalDate localDate) {
        LocalDateTime startKey = sortedStamps.floorKey(minTime);
        if (Objects.isNull(startKey)) {
            LocalDateTime upper = sortedStamps.ceilingKey(minTime);
            if (LocalDateTimes.lessThan(upper, timeConvert.endOf(localDate)))
                startKey = upper;
            else
                return null;
        }

        boolean occ = sortedStamps.get(startKey).occupied;
        while (occ && LocalDateTimes.lessThan(startKey, sortedStamps.lastKey())) {
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
        while (occ && LocalDateTimes.lessThan(current, sortedStamps.lastKey())) {
            current = sortedStamps.higherKey(current);
            if (Objects.isNull(current)) {
                GlobalAssert.that(false);
            }
            occ = sortedStamps.get(current).occupied;// Integer.parseInt(sortedEntries.get(current).get(2));
        }
        return current;
    }

    public HashSet<LocalDate> getLocalDates() {
        return localDates;
    }

    public void printLocalDates() {
        localDates.stream().sorted().forEach(ld -> System.out.println(ld));
    }

    public String getFileName() {
        return fileName;
    }

    public HashMap<LocalDate, Tensor> getDateSplitUp() {
        return dateSplitUp;
    }
}
