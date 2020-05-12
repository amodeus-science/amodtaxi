package ch.ethz.idsc.amodtaxi.scenario.data;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.util.math.SI;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.amodtaxi.trace.TaxiStampHelpers;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.qty.Quantity;
import ch.ethz.idsc.tensor.red.Max;
import ch.ethz.idsc.tensor.red.Min;
import org.matsim.core.router.util.LeastCostPathCalculator;

public abstract class Summary {
    public static Summary of(File file, TaxiStampReader stampReader, FastLinkLookup fastLinkLookup, LeastCostPathCalculator leastCostPathCalculator) throws Exception {
        return FileSummary.of(file, stampReader, fastLinkLookup, leastCostPathCalculator);
    }

    public static Summary of(Summary... summaries) {
        return of(Arrays.asList(summaries));
    }

    public static Summary of(Collection<Summary> summaries) {
        return new CollectedSummary(summaries);
    }

    // ---

    private final Set<File> sources;
    protected final NavigableMap<LocalDate, List<TaxiStamp>> stampsByDay;

    protected Summary(Collection<TaxiStamp> taxiStamps, Set<File> sources) {
        this.sources = sources;
        stampsByDay = taxiStamps.stream().collect(Collectors.groupingBy(
                taxiStamp -> taxiStamp.globalTime.toLocalDate(), //
                TreeMap::new, //
                Collectors.toList()));
    }

    public NavigableSet<LocalDate> dates() {
        return stampsByDay.navigableKeySet();
    }

    public Collection<TaxiStamp> stamps() {
        return stampsByDay.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    }

    public Set<File> sources() {
        return Set.copyOf(sources);
    }

    public Optional<Tensor> boundaries() {
        return TaxiStampHelpers.boundaries(stamps());
    }

    public OptionalDouble minLng() {
        return TaxiStampHelpers.minLng(stamps());
    }

    public OptionalDouble maxLng() {
        return TaxiStampHelpers.maxLng(stamps());
    }

    public OptionalDouble minLat() {
        return TaxiStampHelpers.minLat(stamps());
    }

    public OptionalDouble maxLat() {
        return TaxiStampHelpers.maxLat(stamps());
    }

    public Optional<LocalDateTime> minTime() {
        return TaxiStampHelpers.minTime(stamps());
    }

    public Optional<LocalDateTime> maxTime() {
        return TaxiStampHelpers.maxTime(stamps());
    }

    public Optional<LocalDateTime> minPickupTime() {
        return TaxiStampHelpers.minPickupTime(stamps());
    }

    public Optional<LocalDateTime> maxPickupTime() {
        return TaxiStampHelpers.maxPickupTime(stamps());
    }

    public long numberOfRequests() {
        return TaxiStampHelpers.numberOfRequests(stamps());
    }

    public Scalar totalDistance() {
        return emptyDistance().add(customerDistance());
    }

    public abstract Scalar emptyDistance();

    public abstract Scalar customerDistance();

    protected abstract Optional<Scalar> emptyDistance(LocalDate date);

    protected abstract Optional<Scalar> customerDistance(LocalDate date);

    protected abstract Tensor journeyTimes();

    protected abstract Tensor journeyTimes(LocalDate date);

    public Optional<Scalar> minJourneyTime() {
        return journeyTimes().stream().reduce(Min::of).map(Scalar.class::cast);
    }

    public Optional<Scalar> maxJourneyTime() {
        return journeyTimes().stream().reduce(Max::of).map(Scalar.class::cast);
    }

    public Summary of(LocalDate date) {
        return new Summary(stampsByDay.get(date), sources) {
            @Override
            public Scalar emptyDistance() {
                return Summary.this.emptyDistance(date).orElse(Quantity.of(0, SI.METER));
            }

            @Override
            public Scalar customerDistance() {
                return Summary.this.customerDistance(date).orElse(Quantity.of(0, SI.METER));
            }

            @Override
            protected Optional<Scalar> emptyDistance(LocalDate date) {
                return Optional.of(emptyDistance());
            }

            @Override
            protected Optional<Scalar> customerDistance(LocalDate date) {
                return Optional.of(customerDistance());
            }

            @Override
            public Tensor journeyTimes() {
                return Summary.this.journeyTimes(date);
            }

            @Override
            protected Tensor journeyTimes(LocalDate date) {
                return journeyTimes();
            }
        };
    }

    @Override
    public String toString() {
        String string = "Summary of {" + sources.stream().map(File::getName).collect(Collectors.joining(", ")) + "}\n";
        string += "for dates: {" + dates().stream().map(LocalDate::toString).collect(Collectors.joining(", ")) + "}\n\n";
        string += "requests: " + numberOfRequests() + "\n\n";
        string += "times: " + minTime().map(minTime -> minTime + " - " + maxTime().orElseThrow()).orElse("N/A") + "\n";
        string += "pickup times: " + minPickupTime().map(minTime -> minTime + " - " + maxPickupTime().orElseThrow()).orElse("N/A") + "\n\n";
        string += "latitude: ";
        OptionalDouble optLat = minLat();
        if (optLat.isPresent()) {
            string += "latitude: " + optLat.getAsDouble() + " - " + maxLat().getAsDouble() + "\n";
            string += "longitude: " + minLng().getAsDouble() + " - " + maxLng().getAsDouble() + "\n\n";
        } else {
            string += "latitude: N/A\n";
            string += "longitude: N/A\n\n";
        }
        string += "journey times: " + minJourneyTime().map(minTime -> minTime + " - " + maxJourneyTime().orElseThrow()).orElse("N/A") + "\n\n";
        string += "empty distance: " + emptyDistance() + "\n";
        string += "customer distance: " + customerDistance() + "\n";
        string += "total distance: " + totalDistance();
        return string;
    }
}
