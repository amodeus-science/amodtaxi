/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.util.List;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.amodtaxi.trace.TaxiStampConverted;

public enum TaxiStampConvertedSF implements TaxiStampConverted {
    INSTANCE;

    /** one entry in an SF data file is [lat long occ unixEpochTime],
     * e.g., [37.79655 -122.39521 1 1213035317] */
    @Override
    public TaxiStamp from(List<String> dataFileRow, AmodeusTimeConvert timeConvert) {
        GlobalAssert.that(dataFileRow.size() == 4);
        TaxiStamp taxiStamp = new TaxiStamp();

        /** time */
        taxiStamp.globalTime = timeConvert.getLdt(Integer.parseInt(dataFileRow.get(3)));

        /** occupancy status */
        taxiStamp.occupied = Integer.parseInt(dataFileRow.get(2)) == 1;

        /** coordinate */
        taxiStamp.gps = new Coord( //
                Double.parseDouble(dataFileRow.get(1)), //
                Double.parseDouble(dataFileRow.get(0)));
        return taxiStamp;
    }
}
