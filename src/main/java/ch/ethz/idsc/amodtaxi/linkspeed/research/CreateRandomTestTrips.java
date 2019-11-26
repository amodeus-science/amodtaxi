/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.linkspeed.research;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.util.LeastCostPathCalculator;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedUtils;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.TensorCoords;
import ch.ethz.idsc.amodeus.taxitrip.ExportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.ShortestDurationCalculator;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodtaxi.linkspeed.iterative.LinkSpeedLeastPathCalculator;
import ch.ethz.idsc.amodtaxi.scenario.chicago.ChicagoReferenceFrames;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class CreateRandomTestTrips {

    public static void main(String[] args) throws Exception {

        /** initial data */
        int numberTrips = 100;
        Random random = new Random(10);
        LocalDate someDate = LocalDate.of(2019, 11, 8);
        ReferenceFrame referenceFrame = ChicagoReferenceFrames.CHICAGO;
        File networkFile = new File("/home/oem/data/ChicagoScenario/network.xml.gz");
        File linkSpeedFile = new File("/home/oem/data/chicagotest/randomTraffic");
        Network network = NetworkLoader.fromNetworkFile(networkFile);
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);
        LinkSpeedDataContainer lsData = LinkSpeedUtils.loadLinkSpeedData(linkSpeedFile);

        /** prepare min-time path calculator */
        LeastCostPathCalculator lcpc = LinkSpeedLeastPathCalculator.from(network, db, lsData);
        ShortestDurationCalculator calc = new ShortestDurationCalculator(lcpc, network, db);

        /** create random trips based on traffic situation */
        List<TaxiTrip> trips = new ArrayList<>();
        for (int i = 0; i < numberTrips; ++i) {

            String id = Integer.toString(i);
            String taxiId = Integer.toString(i);

            // random time
            int secondOfDay = random.nextInt(86400);
            int hour = secondOfDay / 3600;
            int minute = (secondOfDay - hour * 3600) / 60;
            int second = (secondOfDay - hour * 3600 - minute * 60);
            LocalTime time = LocalTime.of(hour, minute, second);
            // LocalTime time = LocalTime.of(0, 0, 1);
            LocalDateTime pickupTimeDate = LocalDateTime.of(someDate, time);

            // 2 random locations for pickup and dropoff
            Tensor pickupLoc = null;
            Link pickupLink = null;
            {
                int bound = network.getLinks().size();
                int elemRand = random.nextInt(bound);
                pickupLink = network.getLinks().values().stream().skip(elemRand).findFirst().get();
                Coord linkCoord = pickupLink.getCoord(); // in [m]
                Coord wgs84Coord = referenceFrame.coords_toWGS84().transform(linkCoord);
                pickupLoc = TensorCoords.toTensor(wgs84Coord);

            }

            Tensor dropoffLoc = null;
            Link dropoffLink = null;
            {
                int bound = network.getLinks().size();
                int elemRand = random.nextInt(bound);
                dropoffLink = network.getLinks().values().stream().skip(elemRand).findFirst().get();
                Coord linkCoord = dropoffLink.getCoord(); // in [m]
                Coord wgs84Coord = referenceFrame.coords_toWGS84().transform(linkCoord);
                dropoffLoc = TensorCoords.toTensor(wgs84Coord);

            }

            Scalar driveTime = Quantity.of(calc.computePath(pickupLink, dropoffLink).travelTime, "s");

            // leaving null as currently not used by Richard
            Scalar waitTime = Quantity.of(300, "s");
            Scalar distance = null;

            trips.add(//
                    TaxiTrip.of(id, taxiId, pickupLoc, dropoffLoc, //
                            distance, pickupTimeDate, waitTime, driveTime)//
            );
        }

        /** save the trips */
        File tripFile = new File("/home/oem/data/chicagotest/randomtrips.csv");
        ExportTaxiTrips.toFile(trips.stream(), tripFile);

    }

}
