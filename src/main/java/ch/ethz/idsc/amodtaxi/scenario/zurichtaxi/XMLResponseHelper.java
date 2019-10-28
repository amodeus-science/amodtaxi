package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

/* package */ enum XMLResponseHelper {
    ;

    private final String latId = "lat=\"";
    private final String lonId = "lon=\"";

    public static void extractFirstLatLong(String xmlStr) {
        
        
        JSONObject jo = new JSONObject(xmlStr);

        System.out.println(jo);
        
//        String firstName = (String) jo.get("type"); 
//        System.out.println(firstName);
//        
//        JSONArray ja = (JSONArray) jo.get("features"); 
//        
//        
//        // iterating phoneNumbers 
//        Iterator itr2 = ja.iterator(); 
//        Iterator<Map.Entry> itr1 = address.entrySet().iterator(); 
//          
//        while (itr2.hasNext())  
//        { 
//            itr1 = ((Map) itr2.next()).entrySet().iterator(); 
//            while (itr1.hasNext()) { 
//                Map.Entry pair = itr1.next(); 
//                System.out.println(pair.getKey() + " : " + pair.getValue()); 
//            } 
//        } 
        
        
        
//        System.out.println(obj.getJSONArray("geometry"));

//        xmlStr.contains("lat=");
//        xmlStr.contains("lon=");
//
//        Integer latStart = xmlStr.indexOf("lat=");
//        Integer lonStart = xmlStr.indexOf("lon=");
//
//        List<Char> keepFour = new ArrayList<>();
//        for (char c : xmlStr.toCharArray()) {
//
//        }

    }
    
//    public static void main(String[] args){
//        
//        String test = "abcdasfasf lat=\"52.5487429714954\" lon=\"-1.81602098644987"
//      display_name=\"13\"";
//        
//    }

}
