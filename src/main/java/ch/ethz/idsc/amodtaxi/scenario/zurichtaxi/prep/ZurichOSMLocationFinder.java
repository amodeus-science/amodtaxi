/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.json.JSONObject;

import ch.ethz.idsc.amodtaxi.nominatim.NomatimURL;
import ch.ethz.idsc.amodtaxi.nominatim.NominatimHelper;
import ch.ethz.idsc.amodtaxi.nominatim.NominatimJSON;
import ch.ethz.idsc.tensor.Tensor;

/* package */ class ZurichOSMLocationFinder {

    private final FixedLocation fixedLocation = new FixedLocation();
    private int counter = 0;

    // for printing and debugging
    private String address_record;
    private List<String> elements;
    private List<String> elements3;

    public Tensor getCoords(String address) {
        // for printing
        address_record = address;

        Tensor longLat = fixedLocation.includes(address);
        if (Objects.isNull(longLat)) {

            elements = TaxiAddress.prepare(address);

            // try with all
            {
                String https_url = NomatimURL.build(elements);
                String returnJSONString = NominatimHelper.queryInterface(https_url);
                JSONObject queryJSON = new JSONObject(returnJSONString);
                longLat = NominatimJSON.toCoordinates(queryJSON);
            }

            // try with last 3 elements
            elements3 = new ArrayList<String>();
            if (Objects.isNull(longLat)) {

                for (int i = 0; i < elements.size(); ++i) {
                    if (i >= elements.size() - 3) {
                        elements3.add(elements.get(i));
                    }
                }
                String https_url = NomatimURL.build(elements3);
                String returnJSONString = NominatimHelper.queryInterface(https_url);
                JSONObject queryJSON = new JSONObject(returnJSONString);
                longLat = NominatimJSON.toCoordinates(queryJSON);
            }

        }

        System.out.println("---- " + ++counter + " ----");
        if (Objects.isNull(longLat))
            printInfo();
        return longLat;
    }

    private void printInfo() {
        System.err.println("address:" + address_record);
        System.err.println("elements: " + elements);
        System.err.println("elements3: " + elements3);

    }

}