package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.net.URI;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class JasonTester {

    public static void main(String[] args) throws Exception {

        // creating it

        String https_url = "https://nominatim.openstreetmap.org/search?q="//
                // + queryInsert//
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

        // JSONObject jo = new JSONObject();
        // jo.put("type", "FeatureCollection");
        //
        // {
        // JSONArray ja = new JSONArray();
        // ja.put(Boolean.TRUE);
        // ja.put("lorem ipsum");
        //
        // JSONObject jo2 = new JSONObject();
        // jo2.put("abc", "def");
        // ja.put(jo2);
        //
        // jo.put("features", ja);
        //
        // System.out.println(jo);
        // }
        //
        // // reading from it
        // JSONArray ja = jo.getJSONArray("features");
        // System.out.println(ja);

    }

}
