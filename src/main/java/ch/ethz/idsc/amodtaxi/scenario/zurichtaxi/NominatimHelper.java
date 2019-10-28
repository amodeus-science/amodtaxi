package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

/* package */ enum NominatimHelper {
    ;

    public static String queryInterface(String https_url) {
        String returnString = "";
        URL url;
        try {

            url = new URL(https_url);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();

            // dump all the content
            returnString = NominatimHelper.readInterfaceResponse(con);
            
            con.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return returnString;
    }

    public static String readInterfaceResponse(HttpsURLConnection con) {
        String allOfIt = "";
        if (Objects.nonNull(con)) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String input;
                while ((input = br.readLine()) != null) {
                    allOfIt = allOfIt + input;
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return allOfIt;
    }
}