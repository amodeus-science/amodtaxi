package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.data;

import java.time.LocalDateTime;
import java.util.SortedMap;

import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;

/* package */ enum NumberOfRequests {
    ;

    /** @return number of requests found in @param sortedEntries, a request
     *         is counted when the occupancy status changes from false to true. */
    public static int in(SortedMap<LocalDateTime, TaxiStamp> sortedEntries) {
        int numRequests = 0;
        boolean occPrev = false;
        for (LocalDateTime time : sortedEntries.keySet()) {
            boolean occ = sortedEntries.get(time).occupied;
            if (occ && !occPrev) { // journey has started
                ++numRequests;
            }
            occPrev = occ;
        }
        return numRequests;
    }
}
