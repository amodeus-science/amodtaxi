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
import ch.ethz.idsc.amodeus.util.io.CsvReader;

/* package */ class VehicleTrips {

    private final String statusID = "\"Vermittlungsstatus\"";
    private Set<String> occupiedIDs = new HashSet<>();
    
    // temporary to store trip information. 
    
    

    public VehicleTrips() {
        occupiedIDs.add("Beim Kunden");
        occupiedIDs.add("Besetzt mit Kunden");
    }

    public List<TaxiTrip> fromTrace(NavigableMap<LocalDateTime, CsvReader.Row> trace) {
        List<TaxiTrip> trips = new ArrayList<>();
        
        
        Entry<LocalDateTime, CsvReader.Row> previous = null;
        
        for(Entry<LocalDateTime, CsvReader.Row> entry : trace.entrySet()){
            if(Objects.nonNull(previous)){
                
                boolean nowOccupied = occupied(entry.getValue());
                boolean beforeOccupied = occupied(previous.getValue());
                
                // a trip starts
                if(nowOccupied && !beforeOccupied){
                    
                }
                
                // a trip ends
                if(beforeOccupied && !nowOccupied){
                    
                    Integer id = 
                    
                    TaxiTrip trip = TaxiTrip.of(id, taxiId, pickupLoc, dropoffLoc, distance,//
                            waitTime, pickupDate, duration);
                }
                
                
            }
            previous = entry;
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
