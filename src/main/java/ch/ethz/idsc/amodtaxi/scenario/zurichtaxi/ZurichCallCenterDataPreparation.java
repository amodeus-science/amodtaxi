package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import ch.ethz.idsc.amodeus.util.Duration;
import ch.ethz.idsc.amodeus.util.io.CsvReader;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.qty.Quantity;

public class ZurichCallCenterDataPreparation {

    private File originalFile = new File("/home/clruch/Downloads/allTripsWithSemicolon.csv");
    private File exportFile = new File("/home/clruch/Downloads/tripsJune21.csv");
    private File traceFile = new File("/home/clruch/Downloads/2017-06-21-GPSFahrtstrecken-Protokoll.csv");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
    private String delim = ",";
    private String delimTrace = ";";
    // 21. of June 2017 is a Wednesday
    private int chosenmonth = 6;
    private int chosenday = 21;

    public ZurichCallCenterDataPreparation() throws Exception {

        BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile));
        List<CsvReader.Row> unreadable = new ArrayList<>();
        List<CsvReader.Row> readable = new ArrayList<>();
        ZurichLocationFinder locationFinder = new ZurichLocationFinder(traceFile, delimTrace);

        Consumer<CsvReader.Row> process = r -> {
            try {
                LocalDateTime ldt1 = LocalDateTime.parse(r.get("Erfassung"), dateFormat);
                LocalDateTime ldt2 = LocalDateTime.parse(r.get("Vermittlung"), dateFormat);
                LocalDateTime ldt3 = LocalDateTime.parse(r.get("Fahrtbeginn"), dateFormat);
                LocalDateTime ldt4 = LocalDateTime.parse(r.get("Fahrtende"), dateFormat);

                Scalar vermittlungZeit = Duration.between(ldt1, ldt2);
                Scalar warteZeit = Duration.between(ldt1, ldt3);

                // erfasst am 12. 6.
                if (ldt1.getDayOfMonth() == chosenday && ldt1.getMonthValue() == chosenmonth) {
                    // vermittelt am 12. 6.
                    if (ldt2.getDayOfMonth() == chosenday && ldt2.getMonthValue() == chosenmonth) {
                        // fahrtbeginn am 12. 6.
                        if (ldt3.getDayOfMonth() == chosenday && ldt3.getMonthValue() == chosenmonth) {
                            // vermittlung within short amount of time immediately
                            if (Scalars.lessEquals(vermittlungZeit, Quantity.of(180, "s"))) {
                                // nonzero difference between vermittlung and fahrbegin
                                if (Scalars.lessThan(Quantity.of(0, "s"), warteZeit)) {

                                    String fahrzeug = r.get("Kenng");

                                    Tensor coordsStart = locationFinder.getCoords(fahrzeug, ldt3);
                                    Tensor coordsEnd = locationFinder.getCoords(fahrzeug, ldt4);

                                    // //
                                    System.out.println(coordsStart);
                                    System.out.println(coordsEnd);
                                    // System.out.println("---");
                                    //
                                    // write these to new file
                                    String line = r.toString();
                                    if (Objects.nonNull(coordsStart) && Objects.nonNull(coordsEnd))
                                        writer.write(line + "," + coordsStart.toString().replace(",", ";") + "," + coordsEnd.toString().replace(",", ";") + "\n");
                                    
                                    if (Objects.isNull(coordsStart) && Objects.nonNull(coordsEnd))
                                        writer.write(line + "," + "undef" + "," + coordsEnd.toString().replace(",", ";") + "\n");
                                    
                                    if (Objects.nonNull(coordsStart) && Objects.isNull(coordsEnd))
                                        writer.write(line + "," + coordsStart.toString().replace(",", ";") + "," + "undef" + "\n");
                                    
                                    if (Objects.isNull(coordsStart) && Objects.isNull(coordsEnd))
                                        writer.write(line + "," + "undef" + "," + "undef" + "\n");
                                    
                                }
                            }
                        }
                    }
                }
                readable.add(r);
            } catch (Exception exception) {
                unreadable.add(r);
                // exception.printStackTrace();
                // System.out.println(r);
                // System.exit(1);
            }
        };

        CsvReader reader = new CsvReader(originalFile, delim);
        writer.write(reader.headerLine() + ",WGS84StartTrace" + ",WGS84EndTrace" + "\n");
        reader.rows(process);
        writer.close();

        System.out.println("Unreadable rows: " + unreadable.size());
        System.out.println("Example:" + unreadable.get(0));
        System.out.println("Readable rows: " + readable.size());
        System.out.println("Example:" + readable.get(0));

    }

    // ---
    public static void main(String[] args) throws Exception {
        new ZurichCallCenterDataPreparation();
    }
}
