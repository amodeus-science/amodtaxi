/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.function.Consumer;

import ch.ethz.idsc.amodeus.taxitrip.ExportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.io.CsvReader;

/* package */ class ZurichTraceTripPreparation {

    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("d.M.yyyy H:mm:ss");
    private final File traceFile = new File("/home/clruch/Downloads/2017-06-21-GPSFahrtstrecken-Protokoll.csv");
    private String delimTrace = ";";

    private final File taxiTripFile = new File("/home/clruch/Downloads/traceTaxiTrips");

    //
    private final HashMap<String, NavigableMap<LocalDateTime, CsvReader.Row>> tracemap = new HashMap<>();

    public ZurichTraceTripPreparation() throws Exception {

        // adding all vehicles to individual sets
        Consumer<CsvReader.Row> process = r -> {
            String fahrzeug = r.get("\"Fahrzeug\"");
            LocalDateTime ldt = LocalDateTime.parse(r.get("\"Zeitpunkt\""), dateFormat);
            if (!tracemap.containsKey(fahrzeug))
                tracemap.put(fahrzeug, new TreeMap<>());
            tracemap.get(fahrzeug).put(ldt, r);
        };

        CsvReader reader = new CsvReader(traceFile, delimTrace);
        reader.rows(process);

        // for each vehicle, extract the trips
        List<TaxiTrip> allTrips = new ArrayList<>();
        tracemap.values().forEach(trace -> {
            VehicleTrips vt = new VehicleTrips();
            allTrips.addAll(vt.fromTrace(trace));
        });

        System.out.println("Number of distinct taxis: " + tracemap.size());
        System.out.println("Number of trips:          " + allTrips.size());

        ExportTaxiTrips.toFile(allTrips.stream(), taxiTripFile);
    }

    // --
    public static void main(String[] args) throws Exception {
        new ZurichTraceTripPreparation();
    }

}
