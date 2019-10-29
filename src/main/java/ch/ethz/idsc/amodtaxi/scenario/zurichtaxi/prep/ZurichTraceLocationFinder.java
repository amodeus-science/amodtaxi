/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;
import java.util.function.Consumer;

import ch.ethz.idsc.amodeus.util.Duration;
import ch.ethz.idsc.amodeus.util.io.CsvReader;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class ZurichTraceLocationFinder {

    private final HashMap<String, NavigableMap<LocalDateTime, CsvReader.Row>> tracemap = new HashMap<>();
    // maximum tolereated difference of the trace to be taken into consideration
    private final Scalar tDiffMax = Quantity.of(300, "s");
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d.M.yyyy H:mm:ss");

    public ZurichTraceLocationFinder(File traceFile, String delim) throws Exception {

        // rows are sorted according to the id of the vehicle
        // and according to the time of the trace recording
        Consumer<CsvReader.Row> process = r -> {
            String fahrzeug = r.get("\"Fahrzeug\"");
            LocalDateTime ldt = LocalDateTime.parse(r.get("\"Zeitpunkt\""), dateFormat);
            if (!tracemap.containsKey(fahrzeug))
                tracemap.put(fahrzeug, new TreeMap<>());
            tracemap.get(fahrzeug).put(ldt, r);
        };
        CsvReader reader = new CsvReader(traceFile, delim);
        reader.rows(process);

    }

    public Tensor getCoords(String vehicle, LocalDateTime ldt) {
        if (tracemap.containsKey(vehicle)) {
            Entry<LocalDateTime, CsvReader.Row> closestRow = closestRow(vehicle, ldt);
            // time difference too large
            if (Scalars.lessThan(tDiffMax, Duration.abs(closestRow.getKey(), ldt))) {
                return null;
            }
            // compute coords from trace
            Double breite = Double.parseDouble(closestRow.getValue().get("\"Breitengrad\""));
            Double laenge = Double.parseDouble(closestRow.getValue().get("\"Laengengrad\""));
            return Tensors.vector(breite, laenge);
        }
        return null;
    }

    /** @return closest {@link CsvReader.Row} to @param ldt in the map for the
     * @param vehicle */
    private Entry<LocalDateTime, CsvReader.Row> closestRow(String vehicle, LocalDateTime ldt) {

        Entry<LocalDateTime, CsvReader.Row> entryBelow = tracemap.get(vehicle).floorEntry(ldt);
        Entry<LocalDateTime, CsvReader.Row> entryAbove = tracemap.get(vehicle).higherEntry(ldt);

        CsvReader.Row below = Objects.nonNull(entryBelow) ? //
                entryBelow.getValue() : null;
        CsvReader.Row above = Objects.nonNull(entryAbove) ? //
                entryAbove.getValue() : null;

        // entries below and above -> return closer one
        if (Objects.nonNull(below) && Objects.nonNull(above)) {
            LocalDateTime ldtBelow = entryBelow.getKey();
            LocalDateTime ldtAbove = entryAbove.getKey();

            Scalar dBelow = Duration.abs(ldtBelow, ldt);
            Scalar dAbove = Duration.abs(ldt, ldtAbove);

            if (Scalars.lessThan(dBelow, dAbove))
                return entryBelow;
            else
                return entryAbove;
        }
        // no entry below
        if (Objects.isNull(below) && Objects.nonNull(above))
            return entryAbove;
        // no entry above
        if (Objects.nonNull(below) && Objects.isNull(above))
            return entryBelow;
        // no entry at all
        return null;
    }

}