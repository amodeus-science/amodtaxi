/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.io.ResourceData;

/* package */ class FixedLocation {

    private Map<List<String>, Tensor> locationMap = new HashMap<>();

    public FixedLocation() {
        Tensor data = ResourceData.of("/zurich/zurichlocations.csv");
        data.flatten(0).forEach(t -> {
            Tensor longLat = t.extract(0, 2);
            Tensor keyWords = t.extract(2, t.length());
            List<String> keyList = new ArrayList<>();
            keyWords.flatten(-1).forEach(t2 -> {
                keyList.add(t2.toString());
            });
            locationMap.put(keyList, longLat);
        });

    }

    public Tensor includes(String address) {
        String addressSmall = address.toLowerCase();
        for (Entry<List<String>, Tensor> entry : locationMap.entrySet()) {
            boolean isAMatch = true;
            for (String mustBeIn : entry.getKey()) {
                if (!addressSmall.contains(mustBeIn)) {
                    isAMatch = false;
                    break;
                }
            }
            if (isAMatch)
                return entry.getValue();
        }
        return null;
    }
}
