/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.io.Locate;
import ch.ethz.idsc.amodtaxi.scenario.Pt2MatsimXML;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsReader;
import ch.ethz.idsc.amodtaxi.tripmodif.ChicagoFormatModifier;
import ch.ethz.idsc.tensor.io.DeleteDirectory;

public class TripsReaderChicagoTest {
    private static final String TRIPFILENAME = "tripsChicago.csv";

    @Test
    public void test() throws Exception {
        /* Init */
        File workingDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "test");
        File resourcesDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/testScenario");
        File tripFile = new File(workingDir, TRIPFILENAME);
        Assert.assertTrue(workingDir.exists() || workingDir.mkdir());
        Files.copy(new File(resourcesDir, TRIPFILENAME), tripFile);
        // ChicagoGeoInformation.setup();
        // ScenarioSetup.in(workingDir, resourcesDir);

        /* Run function of interest */
        File preparedFile = new ChicagoFormatModifier().modify(tripFile);
        TaxiTripsReader tripsReader = new OnlineTripsReaderChicago();
        List<TaxiTrip> taxiTrips = tripsReader.getTrips(preparedFile);

        /* Check functionality */
        Assert.assertEquals(taxiTrips.size(), 89);

        /* Clean up */
        Assert.assertTrue(workingDir.exists());
        DeleteDirectory.of(workingDir, 3, 100);
        Assert.assertFalse(workingDir.exists());
    }
}
