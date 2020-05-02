/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.time.LocalDate;
import java.time.Month;

import junit.framework.TestCase;

public class LocalDateConvertTest extends TestCase {

    public void testSimple() {
        LocalDate localDate = LocalDateConvert.ofOptions("2014/11/18");
        assertEquals(localDate.getYear(), 2014);
        assertEquals(localDate.getMonth(), Month.NOVEMBER);
        assertEquals(localDate.getDayOfMonth(), 18);
    }

}
