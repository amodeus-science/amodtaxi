/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.tripfilter;

import java.util.function.Predicate;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;

/* package */ interface ConsciousFilter extends Predicate<TaxiTrip> {

    /** @return number of assessments made */
    public int numTested();

    /** @return number of assessments labeled false */
    public int numFalse();

    /** print a summary to console */
    public void printSummary();

}
