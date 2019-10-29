/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import java.util.Arrays;
import java.util.List;

/* package */ enum TaxiAddress {
    ;

    public static List<String> prepare(String original) {

        String copy = original.toLowerCase();

        // remove ";"
        copy = copy.replace(";", "");

        // remove "GPS"
        copy = copy.replace("gps", "");

        // remove "CH-"
        copy = copy.replace("ch-", "");

        // add city name if not present
        copy = AddCity.ifNeeded(copy);

        // remove duplicate spaces
        copy = RemoveDuplicate.spaces(copy);

        // remove first space if present
        if (copy.toCharArray()[0] == ' ') {
            copy = copy.substring(1);
        }

        // split at spaces
        List<String> individualWords = Arrays.asList(copy.split(" "));

        return individualWords;
    }

}
