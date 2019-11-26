/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;
import ch.ethz.idsc.amodtaxi.trace.TaxiTrail;

/* package */ class AllTaxiTrips {

    /** Usage: {@link Collection}<{@link TaxiTrips}> trips = AllTaxiTrips.in(dayTaxiRecord).on(simulationDate)
     * 
     * @return all {@link TaxiTrip}s found in the {@link DayTaxiRecord} @param dayTaxiRecord with trip start
     *         on a certain {@link LocalDate} */
    public static AllTaxiTrips in(DayTaxiRecord dayTaxiRecord) {
        return new AllTaxiTrips(dayTaxiRecord);
    }

    // --
    private final DayTaxiRecord dayTaxiRecord;

    private AllTaxiTrips(DayTaxiRecord dayTaxiRecord) {
        this.dayTaxiRecord = dayTaxiRecord;
    }

    public Collection<TaxiTrip> on(LocalDate simulationDate) {
        Collection<TaxiTrip> trips = new ArrayList<>();
        for (TaxiTrail taxiTrail : dayTaxiRecord.getTrails()) {
            trips.addAll(taxiTrail.allTripsBeginningOn(simulationDate));
        }
        return trips;
    }
}
