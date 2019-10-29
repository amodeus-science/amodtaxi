/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

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

        locationMap.put(//
                Arrays.asList("kloten", "hotelstrasse", "2780"), //
                Tensors.vector(8.562125, 47.450992));

        locationMap.put(//
                Arrays.asList("busstation", "zwinglihaus", "zurich"), //
                Tensors.vector(8.516810, 47.372000));

        locationMap.put(//
                Arrays.asList("bellerivestrasse/klausstrasse", "zurich"), //
                Tensors.vector(8.549613, 47.358944));

        locationMap.put(//
                Arrays.asList("waidfussweg", "dorfstrasse/hoenggerstrasse", "zurich"), //
                Tensors.vector(8.519481, 47.395162));

        locationMap.put(//
                Arrays.asList("8302", "kloten", "rondell-strasse"), //
                Tensors.vector(8.564343, 47.453338));

        locationMap.put(//
                Arrays.asList("hotel", "twenty", "five", "hours", "langstr"), //
                Tensors.vector(8.528262, 47.380039));

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
