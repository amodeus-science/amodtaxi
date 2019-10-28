package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

import ch.ethz.idsc.tensor.Tensor;

public class ZurichOSMLocationFinder {

    public ZurichOSMLocationFinder() {

    }

    public Tensor getCoords(String address) throws InterruptedException {

        System.out.println("Address:");
        System.out.println(address);
        System.err.println(prepare(address));
        System.out.println("---");

        Thread.sleep(1000);
        // FIXME

        return null;

        // System.out.println("0");
        // if (tracemap.containsKey(vehicle)) {
        // Entry<LocalDateTime, CsvReader.Row> closestRow = closestRow(vehicle, ldt);
        // // time difference too large
        // if (Scalars.lessThan(tDiffMax, Duration.abs(closestRow.getKey(), ldt))) {
        // System.out.println("1");
        // return null;
        // }
        // // compute coords from trace
        // Double breite = Double.parseDouble(closestRow.getValue().get("\"Breitengrad\""));
        // Double laenge = Double.parseDouble(closestRow.getValue().get("\"Laengengrad\""));
        // return Tensors.vector(breite, laenge);
        // }
        // System.out.println("2");
        // return null;
    }

    private static String prepare(String original) {

        String copy = original;

        // remove ";"
        copy = copy.replace(";", "");

        // remove "GPS"
        copy = copy.replace("GPS", "");

        // remove "GPS"
        copy = copy.replace("CH-", "");

        String https_url = urlBuilder(Arrays.asList("135", "pilkington", "avenue", "birmingham"));
        String returnString = NominatimHelper.queryInterface(https_url);

        System.out.println(returnString);

        return copy;
    }

    private static String urlBuilder(List<String> elements) {
        String queryInsert = "";

        for (int i = 0; i < elements.size(); ++i) {
            if (i < elements.size() - 1)
                queryInsert = queryInsert + elements.get(i) + "+";
            else
                queryInsert = queryInsert + elements.get(i);
        }

        String https_url = "https://nominatim.openstreetmap.org/search?q="//
                + queryInsert//
                // + "135+pilkington+avenue,+birmingham"//
                + "&format=xml&polygon=1&addressdetails=1";
        return https_url;
    }

}