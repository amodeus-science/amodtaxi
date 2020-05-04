package ch.ethz.idsc.amodtaxi.scenario;

import java.util.Collection;
import java.util.function.Supplier;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;

public interface TaxiTripsSupplier extends Supplier<Collection<TaxiTrip>> {
    // ---
}
