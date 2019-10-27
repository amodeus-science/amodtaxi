package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import ch.ethz.idsc.amodeus.analysis.UnitSaveUtils;
import ch.ethz.idsc.amodeus.util.Duration;
import ch.ethz.idsc.amodeus.util.io.CsvReader;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Dimensions;
import ch.ethz.idsc.tensor.qty.Quantity;

public class ZurichCallCenterDataExploration {

    private File originalFile = new File("/home/clruch/Downloads/allTripsWithSemicolon.csv");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("M/d/yyyy H:mm");
    private String delim = ",";
    // 21. of June 2017 is a Wednesday
    private int chosenmonth = 6;
    private int chosenday = 21;

    public ZurichCallCenterDataExploration() throws Exception {

        HashSet<String> kennGAll = new HashSet<>();
        HashSet<String> statii = new HashSet<>();
        List<LocalDateTime> erfassung = new ArrayList<>();
        List<LocalDateTime> vermittlung = new ArrayList<>();
        List<LocalDateTime> fahrtbeginn = new ArrayList<>();
        List<LocalDateTime> fahrtende = new ArrayList<>();
        List<CsvReader.Row> unreadable = new ArrayList<>();
        List<CsvReader.Row> readable = new ArrayList<>();

        // erfassung, vermittlung, fahrtbeginn, fahrtende
        Tensor vermittlungsZeiten = Tensors.empty();
        Tensor anfahrtsZeiten = Tensors.empty();
        Tensor warteZeiten = Tensors.empty();
        Tensor fahrZeiten = Tensors.empty();

        Consumer<CsvReader.Row> process = r -> {
            try {

                // STatus
                String status = r.get("Status");
                statii.add(status);

                // dates
                {
                    LocalDateTime ldt1 = LocalDateTime.parse(r.get("Erfassung"), dateFormat);
                    erfassung.add(ldt1);

                    LocalDateTime ldt2 = LocalDateTime.parse(r.get("Vermittlung"), dateFormat);
                    vermittlung.add(ldt2);

                    LocalDateTime ldt3 = LocalDateTime.parse(r.get("Fahrtbeginn"), dateFormat);
                    fahrtbeginn.add(ldt3);

                    LocalDateTime ldt4 = LocalDateTime.parse(r.get("Fahrtende"), dateFormat);
                    fahrtende.add(ldt4);

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

                                        vermittlungsZeiten.append(vermittlungZeit);
                                        anfahrtsZeiten.append(Duration.between(ldt2, ldt3));
                                        warteZeiten.append(warteZeit);
                                        fahrZeiten.append(Duration.between(ldt3, ldt4));

                                        // System.out.println(kennG);

                                        // kennG
                                        String kennG = r.get("Kenng");
                                        kennGAll.add(kennG);

                                        // write these to new file
                                        String line = r.toString();
                                    }
                                }
                            }
                        }
                    }
                }
                readable.add(r);

            } catch (Exception exception) {
                unreadable.add(r);

            }

        };

        CsvReader reader = new CsvReader(originalFile, delim);
        reader.rows(process);

        System.out.println("Unreadable rows: " + unreadable.size());
        System.out.println("Readable rows: " + readable.size());
        System.out.println("---");

        System.out.println("Number of headers: " + reader.sortedHeaders().size());
        System.out.println("Headers:");
        reader.sortedHeaders().stream().forEach(h -> {
            System.out.print(h + " ");
        });
        System.out.println();
        System.out.println("---");

        System.out.println("---");
        System.out.println("Different kennGs (taxis?):");
        System.out.println(kennGAll.size());
        kennGAll.stream().forEach(s -> System.out.print(" ," + s));
        System.out.println();

        System.out.println("---");
        System.out.println("Status:");
        statii.stream().forEach(s -> {
            System.out.println(s);
        });

        System.out.println("Erfassung days:");
        HashMap<LocalDate, Integer> erfassungDates = new HashMap<>();
        erfassung.stream().forEach(ldt -> {
            LocalDate date = ldt.toLocalDate();
            if (!erfassungDates.containsKey(date)) {
                erfassungDates.put(date, 0);
            }

            erfassungDates.put(date, erfassungDates.get(date) + 1);

        });
        erfassungDates.entrySet().forEach(e -> {
            System.out.println(e.getKey() + ": " + e.getValue());
        });
        System.out.println("---");
        System.out.println("Samples in WarteZeiten: " + Dimensions.of(warteZeiten));

        File folder = new File("/home/clruch/Downloads");
        UnitSaveUtils.saveFile(vermittlungsZeiten, "vermittlung", folder);
        UnitSaveUtils.saveFile(anfahrtsZeiten, "anfahrt", folder);
        UnitSaveUtils.saveFile(warteZeiten, "warten", folder);
        UnitSaveUtils.saveFile(fahrZeiten, "fahren", folder);

    }

    // ---
    public static void main(String[] args) throws Exception {
        new ZurichCallCenterDataExploration();
    }
}
