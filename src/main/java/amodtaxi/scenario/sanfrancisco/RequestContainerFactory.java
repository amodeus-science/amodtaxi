package amodtaxi.scenario.sanfrancisco;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

// TODO refactor why does it have two methods instead of one?
/* package */ class RequestContainerFactory {
    private final int requestIndex;
    private final int fromLinkIndex;
    private final int toLinkIndex;
    private final LocalDateTime submissionTime;
    private final AmodeusTimeConvert timeConvert;

    public RequestContainerFactory(int rIndex, int fromLIndex, int toLIndex, //
            LocalDateTime submissionTime, AmodeusTimeConvert timeConvert) {
        GlobalAssert.that(timeConvert.toEpochSec(submissionTime) >= 1211018404 - 4 - 3600 * 3);
        GlobalAssert.that(timeConvert.toEpochSec(submissionTime) <= 1213088836 + 24 * 3600);
        this.requestIndex = rIndex;
        this.fromLinkIndex = fromLIndex;
        this.toLinkIndex = toLIndex;
        this.submissionTime = submissionTime;
        this.timeConvert = timeConvert;
    }

    public RequestContainer create(RequestStatus status, LocalDate simulationDate) {
        RequestContainer rc = new RequestContainer();
        rc.requestIndex = requestIndex;
        rc.fromLinkIndex = fromLinkIndex;
        rc.toLinkIndex = toLinkIndex;
        rc.submissionTime = timeConvert.ldtToAmodeus(submissionTime, simulationDate);
        rc.requestStatus = new HashSet<>(Arrays.asList(status));
        return rc;
    }
}
