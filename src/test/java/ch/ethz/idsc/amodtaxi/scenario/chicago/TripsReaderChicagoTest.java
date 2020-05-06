/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.io.CopyFiles;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsReader;
import ch.ethz.idsc.amodtaxi.scenario.TestDirectories;
import ch.ethz.idsc.amodtaxi.tripmodif.ChicagoFormatModifier;
import ch.ethz.idsc.tensor.io.DeleteDirectory;

public class TripsReaderChicagoTest {
    private static final String TRIPFILENAME = "tripsChicago.csv";

    @BeforeClass
    public static void setUp() throws Exception {
        GlobalAssert.that(TestDirectories.WORKING.mkdirs());
        CopyFiles.now(TestDirectories.CHICAGO.getAbsolutePath(), //
                TestDirectories.WORKING.getAbsolutePath(), Arrays.asList(TRIPFILENAME), true);
    }

    @Test
    public void test() throws Exception {
        /* Init */
        File tripFile = new File(TestDirectories.WORKING, TRIPFILENAME);

        /* Run function of interest */
        File preparedFile = new ChicagoFormatModifier().modify(tripFile);
        TaxiTripsReader tripsReader = new OnlineTripsReaderChicago();
        List<TaxiTrip> taxiTrips = tripsReader.getTrips(preparedFile);

        /* Check functionality */
        Assert.assertEquals(taxiTrips.size(), 89);
    }

    @AfterClass
    public static void cleanUp() throws Exception {
        DeleteDirectory.of(TestDirectories.WORKING, 3, 100);
    }
}
