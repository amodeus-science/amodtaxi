/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Tensors;

public class ZurichTaxiTripReaderTest {

    @Test
    public void test() throws IOException {

        File tripSampleFile = new File(Locate.repoFolder(ZurichTaxiTripReaderTest.class, "amodtaxi"), //
                "resources/zurichTaxiScenario/trips_sample.csv");
        ZurichTaxiTripReader reader = new ZurichTaxiTripReader(",");
        List<TaxiTrip> trips = reader.getTrips(tripSampleFile);

        /** basic tests on id */
        Assert.assertTrue(trips.get(0).localId.equals("19864185"));
        Assert.assertTrue(trips.get(1).localId.equals("19864189"));
        Assert.assertTrue(trips.get(2).localId.equals("19864197"));

        /** ensure that locations are readable tensors */
        Assert.assertTrue(trips.get(0).pickupLoc.divide(RealScalar.of(2))//
                .equals(Tensors.vector(4.269474163518515, 23.6849035)));

        Assert.assertTrue(trips.get(0).dropoffLoc.divide(RealScalar.of(2))//
                .equals(Tensors.vector(4.260893362429025, 23.6796751)));
    }

}
