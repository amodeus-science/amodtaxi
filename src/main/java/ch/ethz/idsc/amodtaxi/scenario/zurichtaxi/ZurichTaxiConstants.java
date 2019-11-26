/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public enum ZurichTaxiConstants {
    ;

    public static DateTimeFormatter callCenterDateFormat = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
    public static LocalDate simualtionDate = LocalDate.of(2017, 6, 21);

}
