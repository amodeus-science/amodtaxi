/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.linkspeed.batch;

import java.time.LocalDate;
import java.util.Objects;

import org.matsim.api.core.v01.network.Link;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;

public enum PathHandlerUtil {
    ;

    public static void validityCheck(TaxiTrip taxiTrip, AmodeusTimeConvert timeConvert, //
            LocalDate simulationDate, FastLinkLookup fll) {
        int tripStart = timeConvert.ldtToAmodeus(taxiTrip.pickupTimeDate, simulationDate);
        int tripEnd = timeConvert.ldtToAmodeus(taxiTrip.dropoffTimeDate, simulationDate);
        int tripDuration = tripEnd - tripStart;
        GlobalAssert.that(tripDuration >= 0);
        GlobalAssert.that(tripStart >= 0);
        GlobalAssert.that(tripEnd >= 0);
        GlobalAssert.that(!taxiTrip.pickupTimeDate.isBefore(simulationDate.atStartOfDay()));
        GlobalAssert.that(!taxiTrip.dropoffTimeDate.isBefore(simulationDate.atStartOfDay()));
        GlobalAssert.that(!taxiTrip.dropoffTimeDate.isBefore(taxiTrip.pickupTimeDate));
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), taxiTrip.driveTime));
        Link pickupLink = fll.linkFromWGS84(taxiTrip.pickupLoc);
        Link dropOffLink = fll.linkFromWGS84(taxiTrip.dropoffLoc);
        Objects.requireNonNull(pickupLink);
        Objects.requireNonNull(dropOffLink);
    }
}
