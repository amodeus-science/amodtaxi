package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;;

public class TaxiAddressTest {

    @Test
    public void test1() {

        String original = ";GPS;; CH-8302 KLOTEN;; Hotelstrasse 2780;";
        List<String> prepared = TaxiAddress.prepare(original);

        Assert.assertTrue(prepared.get(0).equals("8302"));
        Assert.assertTrue(prepared.get(1).equals("kloten"));
        Assert.assertTrue(prepared.get(2).equals("hotelstrasse"));
        Assert.assertTrue(prepared.get(3).equals("2780"));



    }

    
    @Test
    public void test2() {

        String original = ";GPS;; CH-9323 STEINACH;; Bleichestrasse;";
        List<String> prepared = TaxiAddress.prepare(original);

        Assert.assertTrue(prepared.get(0).equals("9323"));
        Assert.assertTrue(prepared.get(1).equals("steinach"));
        Assert.assertTrue(prepared.get(2).equals("bleichestrasse"));

        System.out.println(prepared);

    }

    
    
    


}
