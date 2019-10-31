package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class StaticHelperTest {

    @Test
    public void test() {

        Tensor start = Tensors.vector(1, 2);
        Tensor end = Tensors.vector(1, 2);
        String location = StaticHelper.addLocationIfFound(start, end);
        Assert.assertTrue(location.equals(",{1; 2},{1; 2}"));
    }

}
