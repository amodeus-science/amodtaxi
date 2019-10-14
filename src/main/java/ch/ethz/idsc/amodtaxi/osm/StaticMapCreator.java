package ch.ethz.idsc.amodtaxi.osm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

import org.matsim.pt2matsim.run.Osm2MultimodalNetwork;

public class StaticMapCreator {

    public static void now(File processingDir, String osmData, String amodeusFile, //
            String pt2MatsimSettingsName) throws FileNotFoundException, IOException {

        System.out.println("Downloading open street map data, this may take a while if not already downloaded...");
        File osmFile = new File(processingDir, osmData);
        File scenarioOptions = new File(processingDir, amodeusFile);
        Objects.requireNonNull(scenarioOptions);
        OsmLoader osm = new OsmLoader(scenarioOptions);
        osm.saveIfNotAlreadyExists(osmFile);
        /** generate a network using pt2Matsim */
        // TODO only recreate if not existing
        String pt2MatsimSettings = processingDir.getAbsolutePath() + "/" + pt2MatsimSettingsName;
        System.out.println(pt2MatsimSettings);
        Osm2MultimodalNetwork.run(pt2MatsimSettings);
    }

}
