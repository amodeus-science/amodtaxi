/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.nominatim;

import java.net.URI;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Test;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class NominatimQueryTester {

    @Test
    public void detailedPrintTest() throws Exception {
        // creating it
        String https_url = "https://nominatim.openstreetmap.org/search?q="//
                + "135+pilkington+avenue,+birmingham"//
                + "&format=geojson";

        URI uri = new URI(https_url);
        JSONTokener tokener = new JSONTokener(uri.toURL().openStream());
        JSONObject root = new JSONObject(tokener);

        System.out.println("root:");
        System.out.println(root);
        System.out.println("---");

        System.out.println("ja1");
        Object obj1 = root.get("features");
        JSONArray ja1 = (JSONArray) obj1;
        System.out.println(ja1);
        System.out.println("---");

        System.out.println("jo1");
        JSONObject jo1 = ja1.getJSONObject(0);
        System.out.println(jo1);
        System.out.println("---");

        System.out.println("jo2");
        JSONObject jo2 = jo1.getJSONObject("geometry");
        System.out.println(jo2);
        System.out.println("---");

        System.out.println("ja2");
        JSONArray ja2 = jo2.getJSONArray("coordinates");
        System.out.println(ja2);
        System.out.println("---");

        System.out.println("t1");
        String str1 = ja2.get(0).toString();
        String str2 = ja2.get(1).toString();
        Tensor t1 = Tensors.vector(Double.parseDouble(str1), Double.parseDouble(str2));
        System.out.println(t1);
        System.out.println("---");

        Assert.assertTrue(t1.equals(Tensors.fromString("{-1.8164308339635, 52.5487921}")));
    }

    @Test
    public void simpleTest() throws Exception {

        // creating it
        String https_url = "https://nominatim.openstreetmap.org/search?q="//
                + "1+bundesplatz+bern"//
                + "&format=geojson";

        // receive query
        URI uri = new URI(https_url);
        JSONTokener tokener = new JSONTokener(uri.toURL().openStream());
        JSONObject queryJSON = new JSONObject(tokener);

        // check lat long
        Tensor latLong = NominatimJSON.toCoordinates(queryJSON);
        Assert.assertTrue(latLong.equals(Tensors.fromString("{7.44504019681667, 46.94707525}")));

    }

}
