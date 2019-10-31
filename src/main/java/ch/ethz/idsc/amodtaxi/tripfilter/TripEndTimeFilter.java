/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.tripfilter;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.qty.Quantity;

public class TripEndTimeFilter extends AbstractConsciousFilter {
    private final Scalar maxEndTime;

    public TripEndTimeFilter(Scalar maxEndTime) {
        GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, SI.SECOND), maxEndTime));
        this.maxEndTime = maxEndTime;
    }

    @Override
    public boolean testInternal(TaxiTrip t) {
        // seconds of day when trip starts
        // 15 minutes are added because trips are always projected to start of 15 min interval
        double secStart = 15 * 60.0 + t.pickupTimeDate.getHour() * 3600.0 + //
                t.pickupTimeDate.getMinute() * 60.0 + t.pickupTimeDate.getSecond();

        // end time
        Scalar endTime = Quantity.of(secStart, SI.SECOND).add(t.driveTime);

        // trips which end after the maximum end time are rejected
        boolean afterEnd = Scalars.lessEquals(endTime, maxEndTime);
        if (!afterEnd) {
            System.out.println("Trip removed because it ends after the simulation termination: ");
            System.out.println(t.pickupTimeDate + " with duration " + t.driveTime);
            System.out.println("===");
        }
        return afterEnd;
    }
}
