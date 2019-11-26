/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.population;

import java.time.LocalDateTime;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.io.CsvReader;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ enum TaxiTripParse {
    ;

    public static TaxiTrip fromRow(CsvReader.Row line) {
        // get attributes
        String globalId = line.get("localId");
        String taxiId = line.get("taxiId");
        Tensor pickupLoc = Tensors.fromString(line.get("pickupLoc"));
        Tensor dropoffLoc = Tensors.fromString(line.get("dropoffLoc"));
        Scalar distance = Scalars.fromString(line.get("distance"));
        Scalar waitTime = Scalars.fromString(line.get("waitTime"));
        LocalDateTime pickupDate = LocalDateTime.parse(line.get("pickupDate"));
        Scalar duration = Scalars.fromString(line.get("duration"));

        // compile trip
        return TaxiTrip.of(globalId, taxiId, pickupLoc, dropoffLoc, distance, //
                pickupDate, waitTime, duration);
    }

}
