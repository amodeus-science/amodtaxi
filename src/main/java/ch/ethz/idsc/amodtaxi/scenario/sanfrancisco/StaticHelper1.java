/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;

/* package */ enum StaticHelper1 {
    ;

    public static int getDropOffTime(int timeDriveStart, NavigableMap<Integer, TaxiStamp> sortedMap) {
        GlobalAssert.that(sortedMap.get(timeDriveStart).roboTaxiStatus.equals(RoboTaxiStatus.DRIVEWITHCUSTOMER));
        boolean endJourney = false;
        int dropOffTime = sortedMap.lastKey(); // default value
        int time = sortedMap.higherKey(timeDriveStart);
        while (!endJourney && time < sortedMap.lastKey()) {

            /** check if journey ends */
            if (!sortedMap.get(time).roboTaxiStatus.equals(RoboTaxiStatus.DRIVEWITHCUSTOMER)) {
                dropOffTime = time;
                break;
            }
            time = sortedMap.higherKey(time);
        }
        return dropOffTime;
    }

    /** returns a Map<RequestStatus, Integer> for as many time steps as available
     * assuming that every RequestStatus takes just one time step, example: two
     * time steps in sortedMap before timeDriveStart (=t0) yields:
     * <REQUESTED,null>, <ASSIGNED,null>, <PICKUPDRIVE, t0-2> , <PICKUP,t0-1> */
    public static Map<RequestStatus, LocalDateTime> getRequestTimes(//
            LocalDateTime timeDriveStart, NavigableMap<LocalDateTime, TaxiStamp> sortedMap) {

        Map<RequestStatus, LocalDateTime> map = new LinkedHashMap<>();
        map.put(RequestStatus.REQUESTED, null);
        map.put(RequestStatus.ASSIGNED, null);
        map.put(RequestStatus.PICKUPDRIVE, null);
        map.put(RequestStatus.PICKUP, null);

        LocalDateTime time = timeDriveStart;
        List<LocalDateTime> times = new ArrayList<>();
        int iter = 0;
        while (Objects.nonNull(sortedMap.lowerKey(time)) && iter < 4) {
            times.add(sortedMap.lowerKey(time));
            time = sortedMap.lowerKey(time);
            ++iter;
        }

        List<RequestStatus> reverseOrderedKeys = new ArrayList<>(map.keySet());
        Collections.reverse(reverseOrderedKeys);
        int i = 0;
        for (RequestStatus rqs : reverseOrderedKeys) {
            if (i < times.size()) {
                map.put(rqs, times.get(i));
            }
            ++i;
        }
        map.put(RequestStatus.DRIVING, timeDriveStart);
        return map;
    }

}