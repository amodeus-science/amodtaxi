/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.Pretty;
import ch.ethz.idsc.tensor.io.ResourceData;

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
                Arrays.asList("busstation", "zwinglihaus", "aemtlerstrasse/kalkbreitestrasse"), //
                Tensors.vector(8.516810, 47.372000));

        locationMap.put(//
                Arrays.asList("bellerivestrasse/klausstrasse"), //
                Tensors.vector(8.549613, 47.358944));

        locationMap.put(//
                Arrays.asList("waidfussweg", "dorfstrasse/hoenggerstrasse"), //
                Tensors.vector(8.519481, 47.395162));

        locationMap.put(//
                Arrays.asList("8302", "kloten", "rondell-strasse"), //
                Tensors.vector(8.564343, 47.453338));

        locationMap.put(//
                Arrays.asList("hotel", "twenty", "five", "hours", "langstr"), //
                Tensors.vector(8.528262, 47.380039));

        locationMap.put(//
                Arrays.asList("glattbrugg", "muellackerstrasse"), //
                Tensors.vector(8.568886, 47.436284));

        locationMap.put(//
                Arrays.asList("busstation", "friesenbergstrasse", "31"), //
                Tensors.vector(8.504772, 47.362390));

        locationMap.put(//
                Arrays.asList("glattbrugg", "earhartstrasse"), //
                Tensors.vector(8.565579, 47.424150));

        locationMap.put(//
                Arrays.asList("eth/unispital", "tramstation"), //
                Tensors.vector(8.548319, 47.377340));

        locationMap.put(//
                Arrays.asList("badenerstrasse", "651"), //
                Tensors.vector(8.489315, 47.387517));

        locationMap.put(//
                Arrays.asList("tram/busstation", "irchel"), //
                Tensors.vector(8.545033, 47.396139));

        locationMap.put(//
                Arrays.asList("brauerstrasse", "riet", "9016"), //
                Tensors.vector(9.410335, 47.441845));

        locationMap.put(//
                Arrays.asList("zurich", "46", "austrasse"), //
                Tensors.vector(8.520149, 47.364364));

        locationMap.put(//
                Arrays.asList("glattbrugg", "8152", "hohenbühlstrasse"), //
                Tensors.vector(8.571260, 47.436937));

        locationMap.put(//
                Arrays.asList("riet", "9016", "harzbüchelstrasse"), //
                Tensors.vector(9.399548, 47.436587));

        locationMap.put(//
                Arrays.asList("flueelastrasse", "23", "zurich"), //
                Tensors.vector(8.495652, 47.382385));

        locationMap.put(//
                Arrays.asList("ibis", "hotel", "heidi", "abel-weg", "5"), //
                Tensors.vector(8.558503, 47.415922));

        locationMap.put(//
                Arrays.asList("badenerstrasse", "343", "schuhservice"), //
                Tensors.vector(8.510539, 47.378018));

        locationMap.put(//
                Arrays.asList("general", "guisan-quai", "40"), //
                Tensors.vector(8.535180, 47.362966));

        locationMap.put(//
                Arrays.asList("bahnhof", "kloten", "8302"), //
                Tensors.vector(8.583483, 47.414105));

        locationMap.put(//
                Arrays.asList("max", "bill-platz", "19"), //
                Tensors.vector(8.540827, 47.448093));

        locationMap.put(//
                Arrays.asList("fuchsenstrasse", "riet"), //
                Tensors.vector(9.421232, 47.451451));

        locationMap.put(//
                Arrays.asList("glattbrugg", "mühlegasse"), //
                Tensors.vector(8.573674, 47.431354));

        locationMap.put(//
                Arrays.asList("glattbrugg", "graetzlistrasse"), //
                Tensors.vector(8.578773, 47.427000));

        locationMap.put(//
                Arrays.asList("wohlleb-gasse"), //
                Tensors.vector(8.540737, 47.372201));

        locationMap.put(//
                Arrays.asList("glattbrugg", "vrenikerstrasse"), //
                Tensors.vector(8.578504, 47.426661));

        locationMap.put(//
                Arrays.asList("widder-gasse", "zurich"), //
                Tensors.vector(8.540137, 47.372369));

    }

    public static void main(String[] args) {

        Tensor data = ResourceData.of("/zurich/zurichlocations.csv");
        data.flatten(0).forEach(t -> {
            System.out.println(t);
            Tensor longLat = t.extract(0, 2);
            System.out.println(longLat);

            Tensor keyWords = t.extract(2, t.length());
            System.out.println(keyWords);
            List<String> keyList = new ArrayList<>();
            keyWords.flatten(-1).forEach(t2 -> {
                keyList.add(t2.toString());
            });

            System.out.println(keyList);

            System.out.println("---");

        });

        System.out.println(Pretty.of(data));

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
