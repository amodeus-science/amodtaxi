package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.util.Arrays;
import java.util.List;

/* package */ enum TaxiAddress {
    ;

    public static List<String> prepare(String original) {

        String copy = original.toLowerCase();

        // remove ";"
        copy = copy.replace(";", "");

        // System.out.println("copy 1:<" + copy + ">");

        // remove "GPS"
        copy = copy.replace("gps", "");

        // System.out.println("copy 2:<" + copy + ">");

        // remove "GPS"
        copy = copy.replace("ch-", "");

        // add city name if not present
        copy = AddCity.ifNeeded(copy);

        // System.out.println("copy 3:<" + copy + ">");

        // remove duplicate spaces
        copy = RemoveDuplicate.spaces(copy);
        if (copy.toCharArray()[0] == ' ') {
            copy = copy.substring(1);
        }

        // System.out.println("copy 4:<" + copy + ">");

        // split at spaces
        List<String> individualWords = Arrays.asList(copy.split(" "));

        return individualWords;
    }

}
