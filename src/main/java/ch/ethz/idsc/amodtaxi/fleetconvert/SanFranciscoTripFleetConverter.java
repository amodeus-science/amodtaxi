package ch.ethz.idsc.amodtaxi.fleetconvert;

import java.io.File;
import java.time.LocalDate;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.fleetconvert.TripFleetConverter;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsSuppliers;
import ch.ethz.idsc.amodtaxi.scenario.chicago.ScenarioConstants;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilterCollection;
import ch.ethz.idsc.amodtaxi.tripfilter.TripDurationFilter;
import ch.ethz.idsc.amodtaxi.tripfilter.TripEndTimeFilter;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifier;
import ch.ethz.idsc.tensor.qty.Quantity;
import org.matsim.api.core.v01.network.Network;

public class SanFranciscoTripFleetConverter extends TripFleetConverter {
    public SanFranciscoTripFleetConverter(ScenarioOptions scenarioOptions, Network network, //
            DayTaxiRecord dayTaxiRecord, LocalDate localDate, //
            TaxiDataModifier modifier, TaxiTripFilterCollection finalFilters, //
            File targetDirectory) {
        super(scenarioOptions, network, modifier, finalFilters, TaxiTripsSuppliers.fromDayTaxiRecord(dayTaxiRecord, localDate), targetDirectory);
    }

    @Override
    public void setFilters() {
        /** trips outside the range [150[s], 30[h]] are removed */
        primaryFilter.addFilter(new TripDurationFilter(Quantity.of(150, SI.SECOND), Quantity.of(10800, SI.SECOND)));

        /** trips which end after the maximum end time are rejected */
        primaryFilter.addFilter(new TripEndTimeFilter(ScenarioConstants.maxEndTime));
    }
}
