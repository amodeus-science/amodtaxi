/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.util.Collection;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;

/* package */ enum ReadTraceFiles {
    ;

    public static DayTaxiRecord in(FastLinkLookup fastLinkLookup, Collection<File> trcFls, MatsimAmodeusDatabase db) throws Exception {
        /** part 1: filling with data */
        DayTaxiRecordSF dayTaxiRecord = new DayTaxiRecordSF(db, fastLinkLookup);
        CsvFleetReaderSF reader = new CsvFleetReaderSF(dayTaxiRecord);

        int taxiNum = 0;
        for (File trc : trcFls) {
            System.out.println("Now processing: " + trc.getName());
            reader.populateFrom(trc, taxiNum += 1);
        }

        /** part 2: postprocess trails once filled */
        dayTaxiRecord.processFilledTrails();
        Consistency.checkTrail(dayTaxiRecord.getTrails());
        return dayTaxiRecord;
    }
}
