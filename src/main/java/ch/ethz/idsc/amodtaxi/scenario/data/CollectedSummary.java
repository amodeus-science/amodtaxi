package ch.ethz.idsc.amodtaxi.scenario.data;

import java.time.LocalDate;
import java.util.Collection;
import java.util.stream.Collectors;

import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.alg.Join;

/* package */ class CollectedSummary extends Summary {
    private final Collection<Summary> summaries;

    protected CollectedSummary(Collection<Summary> summaries) {
        super(summaries.stream().map(Summary::stamps).flatMap(Collection::stream).collect(Collectors.toList()), //
                summaries.stream().map(summary -> summary.sources).flatMap(Collection::stream).collect(Collectors.toSet()));
        this.summaries = summaries;
    }

    @Override // from Summary
    public Scalar emptyDistance() {
        return summaries.stream().map(Summary::emptyDistance).reduce(Scalar::add).orElseThrow();
    }

    @Override // from Summary
    public Scalar customerDistance() {
        return summaries.stream().map(Summary::customerDistance).reduce(Scalar::add).orElseThrow();
    }

    @Override // from Summary
    protected Scalar emptyDistance(LocalDate date) {
        return summaries.stream().map(summary -> summary.emptyDistance(date)).reduce(Scalar::add).orElseThrow();
    }

    @Override // from Summary
    protected Scalar customerDistance(LocalDate date) {
        return summaries.stream().map(summary -> summary.customerDistance(date)).reduce(Scalar::add).orElseThrow();
    }

    @Override // from Summary
    public Tensor journeyTimes() {
        return summaries.stream().map(Summary::journeyTimes).reduce(Join::of).orElse(Tensors.empty());
    }

    @Override // from Summary
    protected Tensor journeyTimes(LocalDate date) {
        return summaries.stream().map(summary -> summary.journeyTimes(date)).reduce(Join::of).orElse(Tensors.empty());
    }
}
