package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public enum NominatimJSON {
    ;

    public static Tensor toCoordinates(JSONObject queryJSON) {

        try {
            Object obj1 = queryJSON.get("features");
            JSONArray ja1 = (JSONArray) obj1;
            JSONObject jo1 = ja1.getJSONObject(0);
            JSONObject jo2 = jo1.getJSONObject("geometry");
            JSONArray ja2 = jo2.getJSONArray("coordinates");
            String str1 = ja2.get(0).toString();
            String str2 = ja2.get(1).toString();
            Tensor t1 = Tensors.vector(Double.parseDouble(str1), Double.parseDouble(str2));

            return t1;
        } catch (JSONException exception) {
            exception.printStackTrace();
        }
        return null;
    }

}
