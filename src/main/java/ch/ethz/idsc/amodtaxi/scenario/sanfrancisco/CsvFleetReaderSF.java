/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import ch.ethz.idsc.amodtaxi.trace.CsvFleetReaderInterface;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;
import ch.ethz.idsc.amodtaxi.util.CSVUtils;

/* package */ class CsvFleetReaderSF implements CsvFleetReaderInterface {
    private final DayTaxiRecord dayTaxiRecord;

    public CsvFleetReaderSF(DayTaxiRecord dayTaxiRecord) {
        this.dayTaxiRecord = dayTaxiRecord;
    }

    @Override
    public DayTaxiRecord populateFrom(File trailFile, int taxiNumber) {
        try (BufferedReader br = new BufferedReader(new FileReader(trailFile))) {
            br.lines().map(line -> CSVUtils.csvLineToList(line, " ")).forEach(list -> //
                    dayTaxiRecord.insert(list, taxiNumber, trailFile.getName()));
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("Unable to load trail file: " + trailFile.getName());
        }
        return dayTaxiRecord;
    }
}
