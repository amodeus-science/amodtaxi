/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.osm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;

public enum StaticMapCreator {
    ;

    public static void now(File directory) throws FileNotFoundException, IOException {
        now(directory, ScenarioLabels.osmData, ScenarioLabels.amodeusFile, ScenarioLabels.pt2MatSettings);
    }

    public static void now(File directory, String osmData, String amodeusFile, String pt2MatsimSettingsName) throws FileNotFoundException, IOException {
        System.out.println("Downloading open street map data, this may take a while...");
        File osmFile = new File(directory, osmData);
        OsmLoader osmLoader = OsmLoader.of(new File(directory, amodeusFile));
        osmLoader.saveIfNotAlreadyExists(osmFile);

        /** generate a network using pt2Matsim */
        // TODO only recreate if not existing
        File pt2MatsimSettings = new File(directory, pt2MatsimSettingsName);
        System.out.println(pt2MatsimSettings.getAbsolutePath());
        Osm2MultimodalNetwork.run(pt2MatsimSettings.getAbsolutePath());
    }
}
