/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.linkspeed.iterative;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import ch.ethz.idsc.amodeus.analysis.plot.ColorDataAmodeus;
import ch.ethz.idsc.amodeus.analysis.plot.DiagramSettings;
import ch.ethz.idsc.amodeus.analysis.plot.HistogramPlot;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.io.SaveFormats;
import ch.ethz.idsc.amodtaxi.linkspeed.LinkSpeedsExport;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.img.ColorDataIndexed;

/* package */ enum StaticHelper {
    ;

    public static int startTime(TaxiTrip trip) {
        return trip.pickupDate.getHour() * 3600//
                + trip.pickupDate.getMinute() * 60//
                + trip.pickupDate.getSecond();

    }

    public static int endTime(TaxiTrip trip) {
        return startTime(trip) + trip.duration.number().intValue();
    }

    public static boolean ratioDidImprove(Scalar ratioBefore, Scalar ratioAfter) {
        Scalar s1 = ratioBefore.subtract(RealScalar.ONE).abs();
        Scalar s2 = ratioAfter.subtract(RealScalar.ONE).abs();
        return Scalars.lessThan(s2, s1);
    }

    public static void export(File processingDir, LinkSpeedDataContainer lsData, String nameAdd) {
        /** exporting final link speeds file */
        File linkSpeedsFile = new File(processingDir, "/linkSpeedData" + nameAdd);
        try {
            LinkSpeedsExport.using(linkSpeedsFile, lsData);
        } catch (IOException e) {
            System.err.println("Export of LinkSpeedDataContainer failed: ");
            e.printStackTrace();
        }
    }

    public static void exportRatioMap(File relativeDirectory, Map<TaxiTrip, Scalar> ratioLookupMap, String append) {
        Tensor all = Tensors.empty();
        ratioLookupMap.values().forEach(s -> all.append(s));
        try {
            SaveFormats.MATHEMATICA.save(all, relativeDirectory, "diff" + append);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void plotRatioMap(File relativeDirectory, Tensor ratios, String append) {
        String fileName = ("histogram_" + append);
        String title = "Differences in Link Speeds";
        String xLabel = "networkspeed / duration";
        String yLabel = "% of requests";
        ColorDataIndexed colorDataIndexed = ColorDataAmodeus.indexed("097");
        try {
            HistogramPlot.of( //
                    ratios, RealScalar.of(0.02), true, relativeDirectory, //
                    fileName, title, yLabel, //
                    xLabel, DiagramSettings.WIDTH, DiagramSettings.HEIGHT, colorDataIndexed, //
                    Tensors.vector(0, 20));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
