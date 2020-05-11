/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.util.Duration;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.sca.Sign;

// TODO rename and replace JourneyTimes
/* package */ enum JourneyTimesNew {
    ;

    /** @return a {@link Tensor} containing all the journey times visible in the
     *         data @param sortedEntries, whereas the journey time is the time period
     *         during which the taxi was labeled with status occupied
     * 
     * @throws Exception */
    public static Tensor in(Collection<TaxiStamp> taxiStamps) throws Exception {
        Tensor journeyTimes = Tensors.empty();
        if (taxiStamps.stream().noneMatch(taxiStamp -> taxiStamp.occupied))
            return journeyTimes;

        LinkedList<TaxiStamp> taxiStampsSorted = taxiStamps.stream().sorted(Comparator.comparing(taxiStamp -> taxiStamp.globalTime)) //
                .collect(Collectors.toCollection(LinkedList::new));

        LocalDateTime journeyStart = null;
        boolean occPrev = false;
        Iterator<TaxiStamp> iterator = taxiStampsSorted.iterator();
        TaxiStamp taxiStamp = iterator.next();
        LocalDateTime timePrev = taxiStamp.globalTime;
        do {
            if (taxiStamp.occupied && !occPrev) // journey has started
                journeyStart = timePrev;
            else if (!taxiStamp.occupied && occPrev) { // journey has ended
                GlobalAssert.that(Objects.nonNull(journeyStart));
                Scalar journeyTime = !timePrev.equals(journeyStart) ? //
                        Duration.between(journeyStart, timePrev) : //
                        Duration.between(journeyStart, taxiStamp.globalTime);
                journeyTimes.append(Sign.requirePositive(journeyTime));
                journeyStart = null;
            }
            occPrev = taxiStamp.occupied;
            timePrev = taxiStamp.globalTime;
            taxiStamp = iterator.next();
        } while (iterator.hasNext());
        if (taxiStamp.occupied) { // recordings end
            GlobalAssert.that(Objects.nonNull(journeyStart));
            Scalar journeyTime = Duration.between(journeyStart, taxiStamp.globalTime);
            journeyTimes.append(Sign.requirePositive(journeyTime));
        }
        return journeyTimes;
    }
}
