/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.linkspeed.iterative;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;

import ch.ethz.idsc.amodeus.analysis.plot.ColorDataAmodeus;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.io.SaveFormats;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.linkspeed.LinkSpeedsExport;
import ch.ethz.idsc.tensor.RationalScalar;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Scalars;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Range;
import ch.ethz.idsc.tensor.fig.Histogram;
import ch.ethz.idsc.tensor.fig.VisualSet;
import ch.ethz.idsc.tensor.pdf.BinCounts;

/* package */ enum StaticHelper {
    ;

    private static final int WIDTH = 1000;
    private static final int HEIGHT = 750;

    public static int startTime(TaxiTrip trip) {
        return trip.pickupTimeDate.getHour() * 3600//
                + trip.pickupTimeDate.getMinute() * 60//
                + trip.pickupTimeDate.getSecond();
    }

    public static int endTime(TaxiTrip trip) {
        return startTime(trip) + trip.driveTime.number().intValue();
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
        ratioLookupMap.values().forEach(all::append);
        try {
            SaveFormats.MATHEMATICA.save(all, relativeDirectory, "diff" + append);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void plotRatioMap(File relativeDirectory, Tensor ratios, String append) {
        try {
            /** compute bins */
            Scalar binSize = RationalScalar.of(2, 100);
            Scalar numValues = RationalScalar.of(ratios.length(), 1);
            Tensor bins = BinCounts.of(ratios, binSize);
            bins = bins.divide(numValues).multiply(RealScalar.of(100)); // norm

            VisualSet visualSet = new VisualSet(ColorDataAmodeus.indexed("097"));
            visualSet.add(Range.of(0, bins.length()).multiply(binSize), bins);
            // ---
            visualSet.setPlotLabel("Differences in Link Speeds");
            visualSet.setAxesLabelY("% of requests");
            visualSet.setAxesLabelX("networkspeed / duration");

            JFreeChart jFreeChart = Histogram.of(visualSet, s -> "[" + s.number() + " , " + s.add(binSize).number() + ")");
            CategoryPlot categoryPlot = jFreeChart.getCategoryPlot();
            categoryPlot.getDomainAxis().setLowerMargin(0.0);
            categoryPlot.getDomainAxis().setUpperMargin(0.0);
            categoryPlot.getDomainAxis().setCategoryMargin(0.0);
            categoryPlot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);
            categoryPlot.setDomainGridlinePosition(CategoryAnchor.START);

            File file = new File(relativeDirectory, "histogram_" + append + ".png");
            ChartUtilities.saveChartAsPNG(file, jFreeChart, WIDTH, HEIGHT);
            GlobalAssert.that(file.isFile());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
