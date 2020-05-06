/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.tripmodif;

import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.tensor.io.StringScalarQ;

public class ChicagoAirportBoundaryLinksTest {
    @Test
    public void testSimple() {
        Assert.assertFalse(StringScalarQ.any(ChicagoAirportBoundaryLinks.locations()));
    }
}
