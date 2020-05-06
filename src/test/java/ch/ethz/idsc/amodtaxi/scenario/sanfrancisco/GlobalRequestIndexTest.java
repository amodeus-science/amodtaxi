package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

public class GlobalRequestIndexTest {
    @Test
    public void test() {
        GlobalRequestIndex index = new GlobalRequestIndex();
        int first = index.add(1, 1);
        int second = index.add(1, 2);
        int third = index.add(2, 1);
        int fourth = index.add(2, 2);

        Assert.assertEquals(1, first);
        Assert.assertEquals(2, second);
        Assert.assertEquals(3, third);
        Assert.assertEquals(4, fourth);

        Assert.assertEquals(second, (int) index.add(1, 2));

        Iterator<Integer> actual = index.getGlobalIDs().iterator();
        for (int i = 1; i < 5; i++)
            Assert.assertEquals(i, (int) actual.next());
        Assert.assertTrue(!actual.hasNext());
    }
}
