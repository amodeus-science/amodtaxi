/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.tripfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;

/** Contains a set of filters that process an individual {@link TaxiTrip}
 * and let it pass or not: TaxiTrip -> {true,false} */
public class TaxiTripFilterCollection {
    private final List<ConsciousFilter> filters = new ArrayList<>();

    public final void addFilter(ConsciousFilter filter) {
        filters.add(filter);
    }

    public final Stream<TaxiTrip> filterStream(Stream<TaxiTrip> stream) {
        System.out.println("Number of filters: " + filters.size());
        for (ConsciousFilter dataFilter : filters) {
            System.out.println("Applying " + dataFilter.getClass().getSimpleName() + " on data.");
            stream = stream.filter(dataFilter);
        }

        return stream;
    }

    public void printSummary() {
        for (ConsciousFilter dataFilter : filters)
            dataFilter.printSummary();
    }

}
