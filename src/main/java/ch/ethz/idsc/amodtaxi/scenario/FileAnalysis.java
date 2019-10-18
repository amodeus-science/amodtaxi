/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.analysis.SaveUtils;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.NumberOfRequests;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.tensor.RealScalar;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Quantity;

public class FileAnalysis {
    private final String fileName;
    private final SortedMap<LocalDateTime, TaxiStamp> sortedEntries;
    private boolean tracesInTimeFrame = false;
    private final Map<LocalDate, Tensor> dateSplitUp;

    /** analysis results */
    private int numRequests;
    private LocalDateTime minTime = null;
    private LocalDateTime maxTime = null;
    private Tensor journeyTimes = null;
    private Tensor mapBounds = null;
    private Tensor minMaxJourneyTime = null;

    private Scalar custrDistance = Quantity.of(0, SI.METER);
    private Scalar totalDistance = Quantity.of(0, SI.METER);
    private Scalar emptyDistance = Quantity.of(0, SI.METER);

    private Tensor plotWaitingTimes = null;

    public FileAnalysis(SortedMap<LocalDateTime, TaxiStamp> sortedEntries, MatsimAmodeusDatabase db, //
            Network network, LeastCostPathCalculator leastCostPathCalculator, QuadTree<Link> quadTree, String fileName, //
            Map<LocalDate, Tensor> dateSplitUp) throws Exception {
        this.fileName = fileName;
        this.dateSplitUp = dateSplitUp;
        this.sortedEntries = sortedEntries;
        this.numRequests = 0;
        if (Objects.nonNull(sortedEntries)) {
            this.numRequests = NumberOfRequests.in(sortedEntries);
            if (sortedEntries.size() > 0) {
                tracesInTimeFrame = true;
                this.minTime = sortedEntries.firstKey();
                this.maxTime = sortedEntries.lastKey();
                mapBounds = LongLatRange.in(sortedEntries);
                journeyTimes = JourneyTimes.in(sortedEntries);
                NetworkDistanceHelper dh = new NetworkDistanceHelper(sortedEntries, db, leastCostPathCalculator, quadTree);
                custrDistance = dh.getCustrDistance();
                totalDistance = dh.getTotlDistance();
                emptyDistance = dh.getEmptyDistance();
                plotWaitingTimes = Tensors.empty(); // PlotWaitingTimes.in(sortedEntries);
                GlobalAssert.that(journeyTimes.length() == numRequests);
                minMaxJourneyTime = JourneyTimeRange.in(journeyTimes);
            }
        }
    }

    public int getNumRequests() {
        if (tracesInTimeFrame)
            return numRequests;
        return 0;
    }

    public LocalDateTime getMinTime() {
        return minTime;
    }

    public LocalDateTime getMaxTime() {
        return maxTime;
    }

    public Double getMinLat() {
        return Objects.isNull(mapBounds) ? null : mapBounds.Get(0).number().doubleValue();
    }

    public Double getMaxLat() {
        return Objects.isNull(mapBounds) ? null : mapBounds.Get(1).number().doubleValue();
    }

    public Double getMinLng() {
        return Objects.isNull(mapBounds) ? null : mapBounds.Get(2).number().doubleValue();
    }

    public Double getMaxLng() {
        return Objects.isNull(mapBounds) ? null : mapBounds.Get(3).number().doubleValue();
    }

    public Tensor getJourneyTimes() {
        return journeyTimes;
    }

    public Integer getMinJourneyTime() {
        return Objects.isNull(minMaxJourneyTime) ? null : minMaxJourneyTime.Get(0).number().intValue();
    }

    public Integer getMaxJourneyTime() {
        return Objects.isNull(minMaxJourneyTime) ? null : minMaxJourneyTime.Get(1).number().intValue();
    }

    /** @return {empty distance, customer distance, total distance} */
    public Tensor distances() {
        return Tensors.of(emptyDistance, custrDistance, totalDistance);
    }

    public Tensor getPlotWaitingTimes() {
        return plotWaitingTimes;
    }

    public String getFileName() {
        return fileName;
    }

    public void saveOccProfile(File workingDirectory) throws Exception {
        Tensor profile = Tensors.empty();
        for (LocalDateTime time : sortedEntries.keySet()) {
            Tensor row = Tensors.empty();
            boolean occ = sortedEntries.get(time).occupied;// Integer.parseInt(sortedEntries.get(time).get(2));
            row.append(Tensors.fromString(time.toString()));
            if (occ) {
                row.append(RealScalar.ONE);
            } else {
                row.append(RealScalar.ZERO);
            }
            profile.append(row);
        }
        SaveUtils.saveFile(profile, "occProfile_" + fileName, workingDirectory);
    }

    public Map<LocalDate, Tensor> getDateSplitUp() {
        return dateSplitUp;
    }
}