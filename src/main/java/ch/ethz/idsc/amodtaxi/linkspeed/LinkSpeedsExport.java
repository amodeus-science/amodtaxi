/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.linkspeed;

import java.io.File;
import java.io.IOException;

import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedUtils;


// TODO remove duplicate with amodeus.XYZ.LinkSpeedUtils
public enum LinkSpeedsExport {
    ;

    /** Exports {@link LinkSpeedDataContainer} to the file @param file
     * which was calculated by the {@link TaxiLinkSpeedEstimator} @param lsCalc
     * 
     * @throws IOException */
    public static void using(File file, TaxiLinkSpeedEstimator lsCalc) throws IOException {
        System.out.println("LinkSpeedsFile: " + file.getAbsolutePath());
        LinkSpeedDataContainer lsData = lsCalc.getLsData();
        LinkSpeedUtils.writeLinkSpeedData(file, lsData);
    }

    public static void using(File file, LinkSpeedDataContainer lsData) throws IOException {
        System.out.println("LinkSpeedsFile: " + file.getAbsolutePath());
        LinkSpeedUtils.writeLinkSpeedData(file, lsData);
    }
}
