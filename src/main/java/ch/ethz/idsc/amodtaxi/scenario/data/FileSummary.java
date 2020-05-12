package ch.ethz.idsc.amodtaxi.scenario.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.amodtaxi.util.CSVUtils;
import ch.ethz.idsc.amodtaxi.util.ReverseLineInputStream;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;
import org.matsim.core.router.util.LeastCostPathCalculator;

/* package */ class FileSummary extends Summary {
    public static Summary of(File file, TaxiStampReader stampReader, FastLinkLookup fastLinkLookup, LeastCostPathCalculator leastCostPathCalculator) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ReverseLineInputStream(file)))) {
            Collection<TaxiStamp> taxiStamps = reader.lines().map(line -> CSVUtils.csvLineToList(line, " ")).map(stampReader::read).collect(Collectors.toList());
            return new FileSummary(taxiStamps, file, fastLinkLookup, leastCostPathCalculator);
        }
    }

    // ---

    private final NavigableMap<LocalDate, Tensor> distances;
    private final NavigableMap<LocalDate, Tensor> journeyTimes;

    protected FileSummary(Collection<TaxiStamp> taxiStamps, File file, FastLinkLookup fastLinkLookup, LeastCostPathCalculator leastCostPathCalculator) {
        super(taxiStamps, Collections.singleton(file));
        distances = stampsByDay.entrySet().stream().collect(Collectors.toMap( //
                Map.Entry::getKey, //
                e -> {
                    NetworkDistanceHelperNew distanceHelper = new NetworkDistanceHelperNew(e.getValue(), fastLinkLookup, leastCostPathCalculator);
                    return Tensors.of(distanceHelper.getEmptyDistance(), distanceHelper.getCustomerDistance());
                }, //
                (v1, v2) -> { throw new RuntimeException(); }, //
                TreeMap::new));
        journeyTimes = stampsByDay.entrySet().stream().collect(Collectors.toMap( //
                Map.Entry::getKey, //
                e -> {
                    try {
                        return JourneyTimesNew.in(e.getValue());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        throw new RuntimeException();
                    }
                }, //
                (v1, v2) -> { throw new RuntimeException(); }, //
                TreeMap::new));
    }

    @Override // from Summary
    public Scalar emptyDistance() {
        return distances.values().stream().map(vector -> vector.Get(0)).reduce(Scalar::add).orElseThrow();
    }

    @Override // from Summary
    public Scalar customerDistance() {
        return distances.values().stream().map(vector -> vector.Get(1)).reduce(Scalar::add).orElseThrow();
    }

    @Override // from Summary
    protected Optional<Scalar> emptyDistance(LocalDate date) {
        return Optional.ofNullable(distances.get(date)).map(vector -> vector.Get(0));
    }

    @Override // from Summary
    protected Optional<Scalar> customerDistance(LocalDate date) {
        return Optional.ofNullable(distances.get(date)).map(vector -> vector.Get(1));
    }

    @Override // from Summary
    public Tensor journeyTimes() {
        return journeyTimes.values().stream().reduce(Join::of).orElse(Tensors.empty());
    }

    @Override // from Summary
    protected Tensor journeyTimes(LocalDate date) {
        return journeyTimes.getOrDefault(date, Tensors.empty());
    }
}
