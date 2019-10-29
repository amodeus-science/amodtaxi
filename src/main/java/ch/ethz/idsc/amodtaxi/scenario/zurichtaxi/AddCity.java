package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.util.Arrays;
import java.util.List;

/* package */ enum AddCity {
    ;

    public static String ifNeeded(String original) {
        List<String> knownCities = Arrays.asList("brugg", "zurich", "kloten", "steinach", "baeretswil", //
                "gallen", "gossau", "zürich", "adliswil", "zollikon", "oberrieden", //
                "schlieren", "uitikon", "lufingen", "wallisellen", "volketswil", //
                "riet", "zumikon", "bassersdorf", "wiedikon");

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
