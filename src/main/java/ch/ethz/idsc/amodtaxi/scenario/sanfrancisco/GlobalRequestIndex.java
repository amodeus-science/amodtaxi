/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.util.HashMap;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ class GlobalRequestIndex {
    private int globalReqIndex = 0;
    private HashMap<VehReqPair, Integer> reqMap = new HashMap<>();

    /** adds request for vehicle with index
     * 
     * @param vehicleIndex and its local request index
     * @param vehReqIndex
     * @return associated globalReqIndex */
    public Integer add(int vehicleIndex, int vehReqIndex) {
        VehReqPair vrp = new VehReqPair(vehicleIndex, vehReqIndex);

        if (reqMap.containsKey(vrp)) {
            return reqMap.get(vrp);
        }
        globalReqIndex = reqMap.size() + 1;
        Integer prevVal = reqMap.put(vrp, globalReqIndex);
        GlobalAssert.that(Objects.isNull(prevVal));
        return reqMap.get(vrp);
    }

    public SortedSet<Integer> getGlobalIDs() {
        TreeSet<Integer> globalIDs = new TreeSet<>();
        for (Integer globalID : reqMap.values()) {
            globalIDs.add(globalID);
        }
        return globalIDs;
    }

}
