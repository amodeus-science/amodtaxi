package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.util.List;
import java.util.Objects;

import org.json.JSONObject;

import ch.ethz.idsc.tensor.Tensor;

public class ZurichOSMLocationFinder {

    private final FixedLocation fixedLocation = new FixedLocation();

    public Tensor getCoords(String address) throws InterruptedException {

        System.out.println("Address:");
        System.out.println(address);

        Tensor longLat = fixedLocation.includes(address);
        if (Objects.isNull(longLat)) {

            List<String> elements = TaxiAddress.prepare(address);
            System.out.println("Elements: ");
            System.out.println(elements);
            System.out.println("---");

            String https_url = NomatimURL.build(elements);
            String returnJSONString = NominatimHelper.queryInterface(https_url);
            JSONObject queryJSON = new JSONObject(returnJSONString);
            longLat = NominatimJSON.toCoordinates(queryJSON);

            System.out.println("ReturnJSONString:");
            System.out.println(returnJSONString);

        }

        System.out.println("LongLat:");
        System.out.println(longLat);
        
        Thread.sleep(1000);

        System.out.println("----");

        return longLat;

    }

}