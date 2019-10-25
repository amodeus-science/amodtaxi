/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.fleetconvert;

import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.readers.TaxiTripsReader;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilter;
import ch.ethz.idsc.amodtaxi.tripfilter.TripDurationFilter;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifier;
import ch.ethz.idsc.amodtaxi.tripmodif.TripBasedModifier;
import ch.ethz.idsc.tensor.qty.Quantity;

public class ChicagoTripFleetConverter extends TripFleetConverter {

    public ChicagoTripFleetConverter(ScenarioOptions scenarioOptions, Network network, //
            TripBasedModifier corrector, TaxiDataModifier generalModifier, TaxiTripFilter finalFilters, //
            TaxiTripsReader tripsReader) {
        super(scenarioOptions, network, corrector, generalModifier, finalFilters, tripsReader);
    }

    @Override
    public void setFilters() {
        primaryFilter.addFilter(new TripDurationFilter(Quantity.of(0, SI.SECOND), Quantity.of(20000, SI.SECOND)));
        // TODO trips were redistributed in 15 minutes interval randomly before,
        // add this again if necessary...
        // primaryFilter.addFilter(new DeprcTripNetworkFilter(scenarioOptions, network));
        // primaryFilter.addFilter(new DeprcTripDistanceFilter(Quantity.of(500, SI.METER), Quantity.of(50000, SI.METER)));
    }

}
