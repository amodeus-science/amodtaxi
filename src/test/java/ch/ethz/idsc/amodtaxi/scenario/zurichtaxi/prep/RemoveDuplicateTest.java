/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import org.junit.Assert;
import org.junit.Test;

public class RemoveDuplicateTest {

    @Test
    public void test() {

        String initial = "a      b  cd    d   e";
        String clean = RemoveDuplicate.spaces(initial);
        String should = "a b cd d e";
        Assert.assertTrue(clean.equals(should));

    }

}
