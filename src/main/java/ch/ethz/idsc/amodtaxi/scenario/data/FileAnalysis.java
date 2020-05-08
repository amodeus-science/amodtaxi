/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.SortedMap;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import org.matsim.core.router.util.LeastCostPathCalculator;

import ch.ethz.idsc.amodeus.analysis.SaveUtils;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.qty.Boole;
import ch.ethz.idsc.tensor.qty.Quantity;

public class FileAnalysis {
    private final String fileName;
    private final SortedMap<LocalDateTime, TaxiStamp> sortedEntries;
    private final Map<LocalDate, Tensor> dateSplitUp;

    /** analysis results */
    private int numRequests = 0;
    private LocalDateTime minTime = null;
    private LocalDateTime maxTime = null;
    private Tensor journeyTimes = null;
    private Tensor mapBounds = null;

    private Scalar custrDistance = Quantity.of(0, SI.METER);
    private Scalar totalDistance = Quantity.of(0, SI.METER);
    private Scalar emptyDistance = Quantity.of(0, SI.METER);

    private Tensor plotWaitingTimes = null;

    public FileAnalysis(NavigableMap<LocalDateTime, TaxiStamp> sortedEntries, FastLinkLookup fastLinkLookup, //
            LeastCostPathCalculator leastCostPathCalculator, String fileName, Map<LocalDate, Tensor> dateSplitUp) throws Exception {
        this.fileName = fileName;
        this.dateSplitUp = dateSplitUp;
        this.sortedEntries = sortedEntries;
        if (Objects.nonNull(sortedEntries)) {
            numRequests = NumberOfRequests.in(sortedEntries);
            if (!sortedEntries.isEmpty()) {
                minTime = sortedEntries.firstKey();
                maxTime = sortedEntries.lastKey();
                mapBounds = LongLatRange.in(sortedEntries.values());
                journeyTimes = JourneyTimes.in(sortedEntries);
                GlobalAssert.that(journeyTimes.length() == numRequests);

                plotWaitingTimes = Tensors.empty(); // PlotWaitingTimes.in(sortedEntries);

                NetworkDistanceHelper dh = new NetworkDistanceHelper(sortedEntries, fastLinkLookup, leastCostPathCalculator);
                custrDistance = dh.getCustrDistance();
                totalDistance = dh.getTotlDistance();
                emptyDistance = dh.getEmptyDistance();
            }
        }
    }

    public boolean isEmpty() {
        return sortedEntries.isEmpty();
    }

    public int getNumRequests() {
        return sortedEntries.isEmpty() ? 0 : numRequests;
    }

    public Optional<LocalDateTime> getMinTime() {
        return Optional.ofNullable(minTime);
    }

    public Optional<LocalDateTime> getMaxTime() {
        return Optional.ofNullable(maxTime);
    }

    public Optional<Double> getMinLat() {
        return getMapDimension(0);
    }

    public Optional<Double> getMaxLat() {
        return getMapDimension(1);
    }

    public Optional<Double> getMinLng() {
        return getMapDimension(2);
    }

    public Optional<Double> getMaxLng() {
        return getMapDimension(3);
    }

    private Optional<Double> getMapDimension(int dim) {
        return Optional.ofNullable(mapBounds).map(vector -> vector.Get(dim).number().doubleValue());
    }

    public Optional<Tensor> getJourneyTimes() {
        return Optional.ofNullable(journeyTimes);
    }

    public Optional<Integer> getMinJourneyTime() {
        return Optional.ofNullable(journeyTimes).map(JourneyTimeRange::min).map(Scalar::number).map(Number::intValue);
    }

    public Optional<Integer> getMaxJourneyTime() {
        return Optional.ofNullable(journeyTimes).map(JourneyTimeRange::max).map(Scalar::number).map(Number::intValue);
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
            boolean occ = sortedEntries.get(time).occupied; // Integer.parseInt(sortedEntries.get(time).get(2));
            row.append(Tensors.fromString(time.toString()));
            row.append(Boole.of(occ));
            profile.append(row);
        }
        SaveUtils.saveFile(profile, "occProfile_" + fileName, workingDirectory);
    }

    public Map<LocalDate, Tensor> getDateSplitUp() {
        return dateSplitUp;
    }
}