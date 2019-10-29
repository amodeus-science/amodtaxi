/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import java.util.Arrays;
import java.util.List;

/* package */ enum AddCity {
    ;

    public static String ifNeeded(String original) {
        List<String> knownCities = Arrays.asList("brugg", "zurich", "kloten", "steinach", "baeretswil", //
                "gallen", "gossau", "zürich", "adliswil", "zollikon", "oberrieden", //
                "schlieren", "uitikon", "lufingen", "wallisellen", "volketswil", //
                "riet", "zumikon", "bassersdorf", "wiedikon", "umiken", "hausen", //
                "baden", "bonstetten", "windisch");

        boolean noCity = true;

        for (String city : knownCities) {
            if (original.contains(city)) {
                noCity = false;
                break;
            }
        }

        if (noCity)
            return original + " zurich";
        else
            return original;

    }
}
