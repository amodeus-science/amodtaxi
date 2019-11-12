/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.linkspeed.iterative;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeSet;

import org.matsim.api.core.v01.network.Link;
import org.matsim.core.router.util.LeastCostPathCalculator.Path;

import ch.ethz.idsc.amodeus.linkspeed.LinkIndex;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedTimeSeries;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;

/* package */ enum ApplyScaling {
    ;

    private static boolean allowIncrease = false;

    public static void to(LinkSpeedDataContainer lsData, MatsimAmodeusDatabase db, //
            TaxiTrip trip, Path path, Scalar rescalefactor, int dt) {
        int tripStart = StaticHelper.startTime(trip);
        int tripEnd = StaticHelper.endTime(trip);

        for (Link link : path.links) {
            /** get link properties */
            Integer linkId = LinkIndex.fromLink(db, link);
            double freeSpeed = link.getFreespeed();
            LinkSpeedTimeSeries lsTime = lsData.getLinkMap().get(linkId);

            /** if no recordings are present, initialize with free speed for duration of trip
             * currently, when no recordings are present, the whole day is initiated with freespeed. */

            // initialize relevant timesteps for whole range
            TreeSet<Integer> wholeRange = new TreeSet<>();
            for (int time = 0; time <= 108000; time += dt) {
                wholeRange.add(time);
            }
            // find out which timesteps are relevant for this trip
            TreeSet<Integer> relevantTimes = new TreeSet<>();
            for (int time : wholeRange) {
                if (tripStart <= time && time <= tripEnd) {
                    relevantTimes.add(time);
                }
            }
            // must have at least one entry for convergence
            if (relevantTimes.size() == 0)
                relevantTimes.add(wholeRange.floor(tripStart));
            GlobalAssert.that(relevantTimes.size() > 0);

            // if (Objects.isNull(lsTime)) {
            // // for (int time = tripStart; time <= tripEnd; time += dt) {
            // // lsData.addData(linkId, time, freeSpeed);
            // // }
            // // TODO remove magic const. really necessary all day?
            // // for (int time = 0; time <= 108000; time += dt) {
            // // lsData.addData(linkId, time, freeSpeed);
            // // }
            // for(int time: relevantTimes)
            // }
            // lsTime = lsData.getLinkMap().get(linkId);
            // Objects.requireNonNull(lsTime);

            for (int time : relevantTimes) {
                // check if entry for current relevant timestep is empty (or no entries for current link),
                // and add freespeed as default entry
                if (Objects.isNull(lsTime) || Objects.isNull(lsTime.getSpeedsAt(time))) {
                    lsData.addData(linkId, time, freeSpeed);
                    lsTime = lsData.getLinkMap().get(linkId);
                }
                
                // rescale at current time and update speed
                Scalar speedNow = RealScalar.of(lsTime.getSpeedsAt(time));
                Scalar newSpeedS = speedNow.multiply(rescalefactor);
                double newSpeed = newSpeedS.number().doubleValue();
                if (newSpeed <= link.getFreespeed() || allowIncrease)
                    lsTime.setSpeed(time, newSpeed);
            }
        }
    }
}
