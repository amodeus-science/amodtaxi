/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.data.LocationSpec;
import ch.ethz.idsc.amodeus.data.ReferenceFrame;

public enum ZurichLocationSpecs implements LocationSpec {
    ZURICHTAXI( //
            ZurichReferenceFrames.SWITZERLAND_EPSG, //
            new Coord(2683600.0, 1251400.0)), //
    ;

    private final ReferenceFrame referenceFrame;
    /** increasing the first value goes east
     * increasing the second value goes north */
    private final Coord center;

    private ZurichLocationSpecs(ReferenceFrame referenceFrame, Coord center) {
        this.referenceFrame = referenceFrame;
        this.center = center;
    }

    @Override
    public ReferenceFrame referenceFrame() {
        return referenceFrame;
    }

    @Override
    public Coord center() {
        return center;
    }
}
