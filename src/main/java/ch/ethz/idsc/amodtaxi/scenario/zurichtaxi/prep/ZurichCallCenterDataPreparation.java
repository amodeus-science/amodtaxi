/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import static ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.ZurichTaxiConstants.simualtionDate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import ch.ethz.idsc.amodeus.util.Duration;
import ch.ethz.idsc.amodeus.util.LocalDateTimes;
import ch.ethz.idsc.amodeus.util.io.CsvReader;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.qty.Quantity;

/* package */ class ZurichCallCenterDataPreparation {

    private File originalFile = new File("/home/clruch/Downloads/allTripsWithSemicolon.csv");
    private File exportFile = new File("/home/clruch/Downloads/tripsJune21.csv");
    private File unreadableFile = new File("/home/clruch/Downloads/tripsJune21Unreadable.csv");
    private File traceFile = new File("/home/clruch/Downloads/2017-06-21-GPSFahrtstrecken-Protokoll.csv");

    private String delim = ",";
    private String delimTrace = ";";
    private ZurichTraceLocationFinder traceLocationFinder = new ZurichTraceLocationFinder(traceFile, delimTrace);
    private ZurichOSMLocationFinder osmLocationFinder = new ZurichOSMLocationFinder();
    private BufferedWriter writer = new BufferedWriter(new FileWriter(exportFile));

    // // 21. of June 2017 is a Wednesday
    // private int chosenmonth = 6;
    // private int chosenday = 21;

    public ZurichCallCenterDataPreparation() throws Exception {

        List<CsvReader.Row> unreadable = new ArrayList<>();
        List<CsvReader.Row> readable = new ArrayList<>();

        Consumer<CsvReader.Row> process = r2 -> {

            // try to read the row
            CallCenterRow r = new CallCenterRow(r2);

            // process
            if (r.success) {
                readable.add(r2);
                processReadableLine(r);
            } else {
                unreadable.add(r2);
            }

        };

        System.out.println("KACKE0");

        // create the new trip file
        CsvReader reader = new CsvReader(originalFile, delim);
        writer.write(reader.headerLine() + ",WGS84StartTrace" + ",WGS84EndTrace" + ",WGS84OSMStart" + ",WGS84OSMEnd" + "\n");
        reader.rows(process);
        writer.close();

        // save unreadable rows to other file

        System.out.println("KACKE1");

        BufferedWriter writer2 = new BufferedWriter(new FileWriter(unreadableFile));
        unreadable.forEach(row -> {
            try {
                String line = row.toString() + "\n";
                writer2.write(line);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        writer2.close();

        System.out.println("KACKE2");

        System.out.println("Unreadable rows: " + unreadable.size());
        System.out.println("Example:" + unreadable.get(0));
        System.out.println("Readable rows: " + readable.size());
        System.out.println("Example:" + readable.get(0));

    }

    private void processReadableLine(CallCenterRow r) {

        // Before TODO remove eventually...
        // private int chosenmonth = 6;
        // private int chosenday = 21;

        // 21. of June 2017 is a Wednesday
        int chosenmonth = simualtionDate.getMonthValue();
        int chosenday = simualtionDate.getDayOfMonth();

        // erfasst am 12. 6.
        if (r.ldt1.getDayOfMonth() == chosenday && r.ldt1.getMonthValue() == chosenmonth) {
            // vermittelt am 12. 6.
            if (r.ldt2.getDayOfMonth() == chosenday && r.ldt2.getMonthValue() == chosenmonth) {
                // fahrtbeginn am 12. 6.
                if (r.ldt3.getDayOfMonth() == chosenday && r.ldt3.getMonthValue() == chosenmonth) {

                    if (LocalDateTimes.lessThan(r.ldt2, r.ldt1))
                        System.err.println(r.ldt2 + " < " + r.ldt1);

                    if (LocalDateTimes.lessThan(r.ldt3, r.ldt1))
                        System.err.println(r.ldt3 + " < " + r.ldt1);

                    Scalar vermittlungZeit = Duration.abs(r.ldt1, r.ldt2);
                    Scalar warteZeit = Duration.abs(r.ldt1, r.ldt3);

                    // vermittlung within short amount of time immediately
                    if (Scalars.lessEquals(vermittlungZeit, Quantity.of(180, "s"))) {

                        // nonzero difference between vermittlung and fahrbegin
                        if (Scalars.lessThan(Quantity.of(0, "s"), warteZeit)) {

                            String fahrzeug = r.fahrzeug;// r.get("Kenng");

                            String abfahrt = r.abfahrt;// r.get("1. Abfahrtsadresse");
                            String zielAdd = r.zielAdd;// r.get("Letzte Zieladresse");

                            Tensor coordsStartTrace = traceLocationFinder.getCoords(fahrzeug, r.ldt3);
                            Tensor coordsEndTrace = traceLocationFinder.getCoords(fahrzeug, r.ldt4);

                            Tensor coordsStartOSM = osmLocationFinder.getCoords(abfahrt);
                            Tensor coordsEndOSM = osmLocationFinder.getCoords(zielAdd);

                            // write these to new file
                            String line = r.line;
                            line = line + StaticHelper.addLocationIfFound(coordsStartTrace, coordsEndTrace);
                            line = line + StaticHelper.addLocationIfFound(coordsStartOSM, coordsEndOSM);
                            line = line + "\n";

                            System.out.println("line: " + line);

                            try {
                                writer.write(line);
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }

                        }
                    }
                }
            }
        }
    }

    // ---
    public static void main(String[] args) throws Exception {
        new ZurichCallCenterDataPreparation();
    }
}
