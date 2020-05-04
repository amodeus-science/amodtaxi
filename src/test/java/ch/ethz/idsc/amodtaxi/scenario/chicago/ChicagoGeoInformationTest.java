/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecDatabase;

public class ChicagoGeoInformationTest {

    @Test
    public void testSimple() throws Exception {
        /* Function to check */
        ChicagoGeoInformation.setup();

        /* Check if all LocationSpecs were loaded */
        for (LocationSpec locationSpec : ChicagoLocationSpecs.values()) {
            Assert.assertEquals(LocationSpecDatabase.INSTANCE.fromString(locationSpec.name()), locationSpec);
        }
    }
}
