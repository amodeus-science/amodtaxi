/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;

public class ScenarioSpeedSearcher {

    public static void main(String[] args) {

        // File processingDir = new File("/home/clruch/data/TaxiComparison_SFScenario");
        // Network network = InitialNetworkPreparerSF.run(processingDir);

        Network network = NetworkLoader.fromNetworkFile(new File("/home/clruch/data/TaxiComparison_SFScenario/network.xml"));

        Map<Double, Link> speeds = new HashMap<>();

        network.getLinks().values().forEach(l -> {
            speeds.put(l.getFreespeed(), l);

            String attrb = (String) l.getAttributes().getAsMap().get("osm:way:highway");

            if (attrb.contains("motorway")) {
                System.out.println(attrb + ", " + l.getFreespeed());
            }

        });

        System.out.println("Speeds:");
        speeds.entrySet().forEach(e -> {
            System.out.println("speed: " + e.getKey());
            System.out.println("link:  " + e.getValue().toString());
        });
    }

}
