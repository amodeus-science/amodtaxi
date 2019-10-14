/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.tripfilter;

import java.util.Objects;
import java.util.Random;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;

public class RandomRemoverFilter extends AbstractConsciousFilter {

    private final Random random;
    private final double keepShare;

    public RandomRemoverFilter(Random random, double keepShare) {
        this.random = Objects.requireNonNull(random);
        this.keepShare = keepShare;
    }

    @Override
    public boolean testInternal(TaxiTrip t) {
        if (random.nextDouble() <= keepShare)
            return true;
        return false;
    }
}
