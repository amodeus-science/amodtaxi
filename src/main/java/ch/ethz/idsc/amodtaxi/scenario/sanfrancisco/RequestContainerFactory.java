/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

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
    private final String requestIndex;
    private final int fromLinkIndex;
    private final int toLinkIndex;
    private final LocalDateTime submissionTime;
    private final AmodeusTimeConvert timeConvert;

    public RequestContainerFactory(String rIndex, int fromLIndex, int toLIndex, //
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
        RequestContainer requestContainer = new RequestContainer();
        requestContainer.requestIndex = Integer.parseInt(requestIndex);
        requestContainer.fromLinkIndex = fromLinkIndex;
        requestContainer.toLinkIndex = toLinkIndex;
        requestContainer.submissionTime = timeConvert.ldtToAmodeus(submissionTime, simulationDate);
        requestContainer.requestStatus = new HashSet<>(Arrays.asList(status));
        return requestContainer;
    }
}
