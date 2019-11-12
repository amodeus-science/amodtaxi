package ch.ethz.idsc.amodtaxi.linkspeed.research;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.taxitrip.ImportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodtaxi.linkspeed.iterative.IterativeLinkSpeedEstimator;
import ch.ethz.idsc.amodtaxi.scenario.chicago.ChicagoReferenceFrames;

public class CreateLSDataWithAlgorithm {

    public static void main(String[] args) throws Exception {

        /** load necessary data */
        File tripFile = new File("/home/oem/data/chicagotest/randomtrips.csv");
        int maxIter = 500;
        File processingDir = new File("/home/oem/data/chicagotest");
        ReferenceFrame referenceFrame = ChicagoReferenceFrames.CHICAGO;
        File networkFile = new File("/home/oem/data/ChicagoScenario/network.xml.gz");
        Random random = new Random(10);
        Network network = NetworkLoader.fromNetworkFile(networkFile);
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);

        /** compute lsData with algorithm */
        List<TaxiTrip> finalTrips = ImportTaxiTrips.fromFile(tripFile);
        new IterativeLinkSpeedEstimator(maxIter).compute(processingDir, network, db, finalTrips);

    }
}
