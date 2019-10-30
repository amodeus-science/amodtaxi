package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTripCheck;
import ch.ethz.idsc.amodeus.util.io.CsvReader;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ class VehicleTrips {

    private final String statusID = "\"Vermittlungsstatus\"";
    private Set<String> occupiedIDs = new HashSet<>();
    private int globalReqCount = 0;

    // temporary to store trip information.

    public VehicleTrips() {
        occupiedIDs.add("Beim Kunden");
        occupiedIDs.add("Besetzt mit Kunden");
    }

    public List<TaxiTrip> fromTrace(NavigableMap<LocalDateTime, CsvReader.Row> trace) {
        List<TaxiTrip> trips = new ArrayList<>();

        // all must have same id, so one conversion enough
        String taxiId = trace.firstEntry().getValue().get("\"Fahrzeug\"");
        System.out.println("Processing: " + taxiId);

        Entry<LocalDateTime, CsvReader.Row> previous = null;
        Entry<LocalDateTime, CsvReader.Row> tripStartStamp = null;

        for (Entry<LocalDateTime, CsvReader.Row> currentStamp : trace.entrySet()) {
            if (Objects.nonNull(previous)) {

                boolean nowOccupied = occupied(currentStamp.getValue());
                boolean beforeOccupied = occupied(previous.getValue());

                // a trip starts
                if (nowOccupied && !beforeOccupied) {
                    tripStartStamp = currentStamp;
                }

                // a trip ends
                if (beforeOccupied && !nowOccupied && Objects.nonNull(tripStartStamp)) {
                    String id = "trace_" + Integer.toString((++globalReqCount));
                    
                    // pickup date and time
                    LocalDateTime pickupDate = tripStartStamp.getKey();
                    Double breitePck = Double.parseDouble(tripStartStamp.getValue().get("\"Breitengrad\""));
                    Double laengePck = Double.parseDouble(tripStartStamp.getValue().get("\"Laengengrad\""));
                    Tensor pickupLoc = Tensors.vector(breitePck, laengePck);

                    // dropoff date and time
                    LocalDateTime dropoffDate = currentStamp.getKey();
                    Double breiteDrp = Double.parseDouble(currentStamp.getValue().get("\"Breitengrad\""));
                    Double laengeDrp = Double.parseDouble(currentStamp.getValue().get("\"Laengengrad\""));
                    Tensor dropoffLoc = Tensors.vector(breiteDrp, laengeDrp);
                    
                    // creating trip
                    TaxiTrip trip = TaxiTrip.of(id, taxiId, pickupLoc, dropoffLoc, null, //
                            null, pickupDate, dropoffDate);
                    GlobalAssert.that(TaxiTripCheck.isOfMinimalScope(trip));
                    trips.add(trip);
                }
            }
            previous = currentStamp;
        }
        return trips;
    }

    private boolean occupied(CsvReader.Row row) {
        String status = row.get(statusID);
        if (occupiedIDs.contains(status))
            return true;
        return false;
    }
}