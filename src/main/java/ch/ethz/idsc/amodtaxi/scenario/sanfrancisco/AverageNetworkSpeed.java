/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDate;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.FastAStarLandmarksFactory;

import ch.ethz.idsc.amodeus.routing.CachedNetworkTimeDistance;
import ch.ethz.idsc.amodeus.routing.EasyMinDistPathCalculator;
import ch.ethz.idsc.amodeus.routing.TimeDistanceProperty;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.geo.ClosestLinkSelect;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;

@Deprecated // TODO delete
public class AverageNetworkSpeed {

    private final CachedNetworkTimeDistance calcDistanceCached;
    private final ClosestLinkSelect linkSelect;
    private final LocalDate simulationDate;
    private final AmodeusTimeConvert timeConvert;

    public AverageNetworkSpeed(Network network, ClosestLinkSelect linkSelect, LocalDate simulationDate, //
            AmodeusTimeConvert timeConvert) {
        this.calcDistanceCached = new CachedNetworkTimeDistance(//
                EasyMinDistPathCalculator.prepPathCalculator(network, new FastAStarLandmarksFactory()), 180000.0, //
                TimeDistanceProperty.INSTANCE);
        this.linkSelect = linkSelect;
        this.simulationDate = simulationDate;
        this.timeConvert = timeConvert;
    }

    public boolean isBelow(TaxiTrip taxiTrip, Scalar meterPerSeconds) {
        Link pickupLocation = linkSelect.linkFromWGS84(taxiTrip.pickupLoc);
        Link drpoffLocation = linkSelect.linkFromWGS84(taxiTrip.dropoffLoc);
        int pickupTime = timeConvert.ldtToAmodeus(taxiTrip.pickupDate, simulationDate);

        Scalar distance = calcDistanceCached.distance(pickupLocation, drpoffLocation, pickupTime);
        Scalar averageSpeed = distance.divide(taxiTrip.duration);

        System.err.println("averageSpeed: " + averageSpeed);

        if (Scalars.lessEquals(averageSpeed, meterPerSeconds))
            return true;
        return false;

    }

}
