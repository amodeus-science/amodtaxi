/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.util.List;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;

/* package */ enum ReadTraceFiles {
    ;

    public static DayTaxiRecord in( //
            FastLinkLookup fastLinkLookup, //
            List<File> trcFls, //
            MatsimAmodeusDatabase db) throws Exception {
        /** part 1: filling with data */
        // List<File> trailFilesComplete = (new MultiFileReader(dataDirectory, "new_")).getFolderFiles();
        DayTaxiRecordSF dayTaxiRecord = new DayTaxiRecordSF(db, fastLinkLookup);
        CsvFleetReaderSF reader = new CsvFleetReaderSF(dayTaxiRecord);

        for (int taxiNum = 0; taxiNum < trcFls.size(); taxiNum++) {
            System.out.println("Now processing: " + trcFls.get(taxiNum).getName());
            reader.populateFrom(trcFls.get(taxiNum), taxiNum + 1);
        }

        /** part 2: postprocess trails once filled */
        dayTaxiRecord.processFilledTrails();
        Consistency.checkTrail(dayTaxiRecord.getTrails());
        return dayTaxiRecord;
    }

}
