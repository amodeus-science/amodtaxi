/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.time.LocalDate;
import java.time.Month;

import org.junit.Assert;
import org.junit.Test;

public class LocalDateConvertTest {
    @Test
    public void testSimple() {
        LocalDate localDate = LocalDateConvert.ofOptions("2014/11/18");
        Assert.assertEquals(localDate.getYear(), 2014);
        Assert.assertEquals(localDate.getMonth(), Month.NOVEMBER);
        Assert.assertEquals(localDate.getDayOfMonth(), 18);
    }

}
