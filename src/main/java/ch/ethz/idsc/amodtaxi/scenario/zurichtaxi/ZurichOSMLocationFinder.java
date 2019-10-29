package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.JSONObject;

import ch.ethz.idsc.tensor.Tensor;

public class ZurichOSMLocationFinder {

    private final FixedLocation fixedLocation = new FixedLocation();
    private int counter = 0;

    public Tensor getCoords(String address) throws InterruptedException {

        // System.out.println("Address:");
        // System.out.println(address);

        Tensor longLat = fixedLocation.includes(address);
        if (Objects.isNull(longLat)) {

            List<String> elements = TaxiAddress.prepare(address);

            // try with all
            {
                // System.out.println("Elements: ");
                // System.out.println(elements);
                String https_url = NomatimURL.build(elements);
                String returnJSONString = NominatimHelper.queryInterface(https_url);
                JSONObject queryJSON = new JSONObject(returnJSONString);
                longLat = NominatimJSON.toCoordinates(queryJSON);
                // System.out.println("ReturnJSONString:");
                // System.out.println(returnJSONString);
            }

            // try with last 3 elements
            List<String> elements3 = new ArrayList<String>();
            if (Objects.isNull(longLat)) {

                for (int i = 0; i < elements.size(); ++i) {
                    if (i >= elements.size() - 3) {
                        elements3.add(elements.get(i));
                    }
                }

                // System.out.println("Elements3: ");
                // System.out.println(elements3);
                String https_url = NomatimURL.build(elements3);
                String returnJSONString = NominatimHelper.queryInterface(https_url);
                JSONObject queryJSON = new JSONObject(returnJSONString);
                longLat = NominatimJSON.toCoordinates(queryJSON);
                // System.out.println("ReturnJSONString:");
                // System.out.println(returnJSONString);
            }

            if (Objects.isNull(longLat)) {
                System.err.println("address:" + address);
                System.err.println("elements: " + elements);
                System.err.println("elements3: " + elements3);
            }

        }

        if (Objects.isNull(longLat)) {
            System.err.println("address:" + address);
        }

        // System.out.println("LongLat:");
        // System.out.println(longLat);

        System.out.println("---- " + ++counter + " ----");

        return longLat;

    }

}