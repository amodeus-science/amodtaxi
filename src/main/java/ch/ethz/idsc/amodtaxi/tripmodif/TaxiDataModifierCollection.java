/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.tripmodif;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.idsc.amodeus.taxitrip.ExportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.ImportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;

public class TaxiDataModifierCollection implements TaxiDataModifier {

    private final List<TripModifier> modifiers = new ArrayList<>();

    public void addModifier(TripModifier modifier) {
        modifiers.add(modifier);
    }

    @Override // from TaxiDataModifier
    public final File modify(File taxiData) throws Exception {

        /** gather all original trips */
        List<TaxiTrip> originals = new ArrayList<>();
        ImportTaxiTrips.fromFile(taxiData).forEach(tt -> originals.add(tt));

        /** notify about all the taxi trips */
        originals.forEach(taxiTrip -> {
            for (TripModifier modifier : modifiers)
                modifier.notify(taxiTrip);
        });

        /** let modifiers do modifications on each trip, then return */
        List<TaxiTrip> modified = new ArrayList<>();
        originals.forEach(taxiTrip -> {
            TaxiTrip changed = taxiTrip;
            for (TripModifier tripModifier : modifiers)
                changed = tripModifier.modify(changed);
            modified.add(changed);
        });
        File outFile = new File(taxiData.getAbsolutePath().replace(".csv", "_modified.csv"));
        ExportTaxiTrips.toFile(modified.stream(), outFile);
        return outFile;
    }

}
