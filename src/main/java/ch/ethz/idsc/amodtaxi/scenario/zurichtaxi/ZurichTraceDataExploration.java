package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.function.Consumer;

import ch.ethz.idsc.amodeus.util.io.CsvReader;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class ZurichTraceDataExploration {

    public static void main(String[] args) throws Exception {

        File file = new File("/home/clruch/Downloads/2017-06-21-GPSFahrtstrecken-Protokoll.csv");

        DateTimeFormatter dateFormat = //
                DateTimeFormatter.ofPattern("M/d/yyyy H:mm");

        String delim = ";";

        List<Integer> allTaxisInCallCenter = Arrays.asList(907, 908, 232, 111, 233, 235, 236, 115, 116, 117, 118, 913, 14, 16, 19, 360, 121, 122, 243, 246, 4, 247, 126, 248, 127,
                7, 249, 129, 22, 24, 26, 27, 371, 250, 130, 372, 131, 252, 133, 254, 255, 134, 377, 256, 378, 136, 257, 258, 137, 379, 259, 138, 32, 33, 35, 36, 38, 39, 380, 381,
                140, 383, 262, 142, 384, 263, 264, 144, 146, 388, 147, 389, 149, 701, 702, 703, 704, 705, 40, 706, 41, 42, 707, 709, 44, 47, 48, 391, 151, 152, 273, 394, 274, 153,
                397, 155, 156, 157, 158, 710, 711, 712, 713, 714, 715, 50, 51, 716, 717, 53, 54, 55, 59, 160, 282, 163, 166, 169, 605, 61, 606, 62, 607, 608, 609, 66, 68, 69, 170,
                171, 292, 172, 174, 175, 179, 610, 611, 612, 71, 615, 72, 73, 617, 74, 75, 619, 180, 182, 185, 187, 188, 189, 622, 501, 504, 81, 82, 83, 506, 84, 508, 85, 87, 88,
                89, 190, 191, 192, 197, 199, 511, 512, 91, 92, 93, 94, 95, 96, 97, 98, 99, 408, 409, 411, 413, 416, 419, 308, 432, 320, 202, 204, 205, 206, 208, 209, 331, 211, 332,
                212, 333, 334, 213, 214, 215, 216, 217, 338, 339, 340, 220, 100, 221, 222, 101, 102, 223, 224, 104, 225, 105, 227, 106, 228, 229, 901, 902, 904, 905, 906);

        HashSet<Integer> allTaxisSet = new HashSet<>();
        allTaxisInCallCenter.stream().forEach(s -> allTaxisSet.add(s));

        GlobalAssert.that(allTaxisInCallCenter.size() == allTaxisSet.size());

        HashSet<Integer> contained = new HashSet<>();
        HashSet<Integer> notContained = new HashSet<>();

        Consumer<CsvReader.Row> process = r -> {

            // System.out.println(r);

            String fahrzeug = r.get("\"Fahrzeug\"");
            // System.out.println(fahrzeug);
            if (fahrzeug.equals("714"))
                System.out.println(r);

            Integer fahrzeugInt = Integer.parseInt(fahrzeug);

            if (allTaxisSet.contains(fahrzeugInt)) {
                contained.add(fahrzeugInt);
            } else {
                notContained.add(fahrzeugInt);
            }

        };

        CsvReader reader = new CsvReader(file, delim);
        reader.rows(process);

        reader.sortedHeaders().forEach(s -> {
            System.out.println(s);
        });

        System.out.println("containd: " + contained.size());
        System.out.println("not contained: " + notContained.size());

    }

}
