package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.data;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.SortedMap;

import ch.ethz.idsc.amodeus.util.Duration;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ enum JourneyTimes {
    ;

    /** @return a {@link Tensor} containing all the journey times visible in the
     *         data @param sortedEntries, whereas the journey time is the time period
     *         during which the taxi was labeled with status occupied
     * 
     * @throws Exception */
    public static Tensor in(SortedMap<LocalDateTime, TaxiStamp> sortedEntries) throws Exception {
        Tensor journeyTimes = Tensors.empty();
        LocalDateTime journeyStart = null;
        boolean occPrev = false;
        LocalDateTime timePrev = sortedEntries.firstKey();
        for (LocalDateTime time : sortedEntries.keySet()) {
            boolean occ = sortedEntries.get(time).occupied;
            if (occ && !occPrev) /** journey has started */
                journeyStart = timePrev;
            if (!occ && occPrev) { /** journey has ended */
                GlobalAssert.that(Objects.nonNull(journeyStart));
                GlobalAssert.that(!occ);
                GlobalAssert.that(occPrev);
                Scalar journeyTime = !timePrev.equals(journeyStart) ? //
                        Duration.between(journeyStart, timePrev) : //
                        Duration.between(journeyStart, time);
                GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, "s"), journeyTime));
                journeyTimes.append(journeyTime);
                journeyStart = null;
            }
            if (occ && time == sortedEntries.lastKey()) {/** recordings end */
                GlobalAssert.that(Objects.nonNull(journeyStart));
                Scalar journeyTime = Duration.between(journeyStart, time);
                GlobalAssert.that(Scalars.lessEquals(Quantity.of(0, "s"), journeyTime));
                journeyTimes.append(journeyTime);
                journeyStart = null;
            }
            occPrev = occ;
            timePrev = time;
        }
        return journeyTimes;
    }
}
