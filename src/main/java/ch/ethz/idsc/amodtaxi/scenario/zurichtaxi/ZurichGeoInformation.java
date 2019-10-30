/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.LocationSpecDatabase;

/* package */ enum ZurichGeoInformation {
    ;

    public static void setup() {
        for (LocationSpec locationSpec : ZurichLocationSpecs.values())
            LocationSpecDatabase.INSTANCE.put(locationSpec);
    }
}
