package amodtaxi.scenario.sanfrancisco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class CsvFleetReaderSF implements CsvFleetReaderInterface {

    protected final DayTaxiRecord dayTaxiRecord;

    public CsvFleetReaderSF(DayTaxiRecord dayTaxiRecord) {
        this.dayTaxiRecord = dayTaxiRecord;
    }

    @Override
    public DayTaxiRecord populateFrom(File trailFile, int taxiNumber) throws Exception {

        try (BufferedReader br = new BufferedReader(new FileReader(trailFile))) {
            String read;
            while (Objects.nonNull(read = br.readLine())) {
                List<String> list = CSVUtils.csvLineToList(read, " ");
                dayTaxiRecord.insert(list, taxiNumber, trailFile.getName());
            }
        } catch (IOException exception) {
            exception.printStackTrace();
            System.err.println("could not load trail file: " + trailFile.getName());

        }

        return dayTaxiRecord;
    }
}
