package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.NavigableMap;

import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.tensor.Scalar;

public enum TaxiTripFinder {
    ;

    /** @return {@link Collection} with all {@link TaxiTrip}s found in the @param timeTaxiStamps
     *         containing the recorded steps for taxi with @param taxiId
     * 
     * @throws Exception */
    public static Collection<TaxiTrip> in(NavigableMap<LocalDateTime, TaxiStamp> timeTaxiStamps, //
            String taxiId) throws Exception {
        List<TaxiTrip> taxiTrips = new ArrayList<>();
        int requestIndex = 0;
        boolean occLast = false;
        TaxiStamp stampStart = null;
        TaxiStamp stampEnd = null;
        for (Entry<LocalDateTime, TaxiStamp> entry : timeTaxiStamps.entrySet()) {
            boolean occNow = entry.getValue().occupied;
            if (occNow && !occLast) /** driving started */
                stampStart = entry.getValue();
            if ((occLast && !occNow) || //
                    (entry.getKey().equals(timeTaxiStamps.lastKey()) && occNow)) { /** driving ended */
                stampEnd = entry.getValue();
                Scalar distance = null;
                Scalar waitTime = null;
                LocalDateTime pickupDateTime = stampStart.globalTime;
                LocalDateTime dropOffDateTime = stampEnd.globalTime;
                TaxiTrip taxiTrip = TaxiTrip.of(requestIndex, taxiId, TensorCoords.toTensor(stampStart.gps), TensorCoords.toTensor(stampEnd.gps), //
                        distance, waitTime, pickupDateTime, dropOffDateTime);
                taxiTrips.add(taxiTrip);
                ++requestIndex;
            }
            occLast = occNow;
        }
        return taxiTrips;
    }
}
