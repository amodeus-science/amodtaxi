package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import ch.ethz.idsc.amodeus.util.io.CsvReader;

/* package */ class CallCenterRow {

    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
    // --

    public LocalDateTime ldt1;
    public LocalDateTime ldt2;
    public LocalDateTime ldt3;
    public LocalDateTime ldt4;
    public String fahrzeug;
    public String abfahrt;
    public String zielAdd;
    public String line;
    public boolean success = false;

    public CallCenterRow(CsvReader.Row r) {

        try {
            ldt1 = LocalDateTime.parse(r.get("Erfassung"), dateFormat);
            ldt2 = LocalDateTime.parse(r.get("Vermittlung"), dateFormat);
            ldt3 = LocalDateTime.parse(r.get("Fahrtbeginn"), dateFormat);
            ldt4 = LocalDateTime.parse(r.get("Fahrtende"), dateFormat);
            fahrzeug = r.get("Kenng");
            abfahrt = r.get("1. Abfahrtsadresse");
            zielAdd = r.get("Letzte Zieladresse");
            success = true;
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("ArrayIndexOutOfBoundsException, typically no start and end times...");
            // System.err.println(r.toString());
        } catch (DateTimeParseException e) {
            System.err.println("DateTimeParseException, typically no start time...");
            // System.err.println(r.toString());
        }

        line = r.toString();
    }
}
