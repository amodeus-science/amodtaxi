package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import java.util.Objects;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.ethz.idsc.tensor.Tensor;

public class FixedLocationTest {

    private static FixedLocation fixedLocation;

    @Before
    public void prepared() {
        fixedLocation = new FixedLocation();
    }

    @Test
    public void test1() {

        String address = "GPS; CH-8302 KLOTEN; Hotelstrasse 2780 ";
        Tensor location = fixedLocation.includes(address);
        Assert.assertTrue(Objects.nonNull(location));
    }

    @Test
    public void test2() {
        String address = "BUSSTATION FRIESENBERGSTRASSE; FRIESENBERGSTRASSE/SCHWEIGHOFSTRASSE";
        Tensor location = fixedLocation.includes(address);
        Assert.assertTrue(Objects.nonNull(location));
    }

    @Test
    public void test3() {
        String address = "; BADENERSTRASSE 651";
        Tensor location = fixedLocation.includes(address);
        Assert.assertTrue(Objects.nonNull(location));
    }

}
