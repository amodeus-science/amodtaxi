package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.util.List;

/* package */ enum NomatimURL {
    ;
    
    public static String build(List<String> elements) {
        String queryInsert = "";

        for (int i = 0; i < elements.size(); ++i) {
            if (i < elements.size() - 1)
                queryInsert = queryInsert + elements.get(i) + "+";
            else
                queryInsert = queryInsert + elements.get(i);
        }

        // NOW JSON
        String https_url = "https://nominatim.openstreetmap.org/search?q="//
                + queryInsert//
                // + "135+pilkington+avenue,+birmingham"//
                + "&format=geojson";
        return https_url;

    }

}
