/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecDatabase;
import junit.framework.TestCase;

public class ChicagoGeoInformationTest extends TestCase {
    public void testSimple() throws Exception {
        /* Function to check */
        ChicagoGeoInformation.setup();

        /* Check if all LocationSpecs were loaded */
        for (LocationSpec locationSpec : ChicagoLocationSpecs.values())
            assertEquals(LocationSpecDatabase.INSTANCE.fromString(locationSpec.name()), locationSpec);
    }
}
