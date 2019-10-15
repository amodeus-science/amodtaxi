/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.text.ParseException;
import java.time.LocalDate;

public enum LocalDateConvert {
    ;

    public static LocalDate ofOptions(String dateString) throws ParseException {
        String[] split = dateString.split("/");
        LocalDate lDate = LocalDate.of(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        return lDate;
    }

    // TODO move to test
    // public static void main(String[] args) throws ParseException{
    // ofOptions("2014/11/18");
    // }

}
