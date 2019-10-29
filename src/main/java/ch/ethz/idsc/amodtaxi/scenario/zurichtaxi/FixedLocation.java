package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ class FixedLocation {

    private Map<List<String>, Tensor> locationMap = new HashMap<>();

    public FixedLocation() {

        locationMap.put(//
                Arrays.asList("kantonsspital", "st", "gallen"), //
                Tensors.vector(9.390619, 47.430326));

        locationMap.put(//
                Arrays.asList("hotel", "grand", "dolder"), //
                Tensors.vector(8.573895, 47.372712));

        locationMap.put(//
                Arrays.asList("hotel", "storchen", "weinplatz"), //
                Tensors.vector(8.541645, 47.371404));

        locationMap.put(//
                Arrays.asList("swisscasino", "gessnerallee"), //
                Tensors.vector(8.533354, 47.373745));

        locationMap.put(//
                Arrays.asList("airport", "kloten"), //
                Tensors.vector(8.562125, 47.450992));
        
        locationMap.put(//
                Arrays.asList("kloten", "vorfahrt"), //
                Tensors.vector(8.562125, 47.450992));

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
