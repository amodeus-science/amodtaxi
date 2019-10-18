package ch.ethz.idsc.amodtaxi.tripmodif;

import ch.ethz.idsc.tensor.io.StringScalarQ;
import junit.framework.TestCase;

public class ChicagoAirportBoundaryLinksTest extends TestCase {
    public void testSimple() {
        assertFalse(StringScalarQ.any(ChicagoAirportBoundaryLinks.locations()));
    }
}
