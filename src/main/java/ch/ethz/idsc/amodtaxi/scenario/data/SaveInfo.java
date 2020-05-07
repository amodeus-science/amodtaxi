/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.data;

import java.io.BufferedWriter;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

/* package */ enum SaveInfo {
    ;

    public static void of(Collection<FileAnalysis> filesAnalysis, BufferedWriter out, //
            File saveSubDir, AmodeusTimeConvert timeConvert) throws Exception {
        /** number of requests */
        int numRequests = filesAnalysis.stream().mapToInt(FileAnalysis::getNumRequests).sum();

        /** distances */
        Scalar custrDistance = filesAnalysis.stream().map(FileAnalysis::distances).map(vector -> vector.Get(0)).reduce(Scalar::add).orElseThrow();
        Scalar totalDistance = filesAnalysis.stream().map(FileAnalysis::distances).map(vector -> vector.Get(1)).reduce(Scalar::add).orElseThrow();
        Scalar emptyDistance = filesAnalysis.stream().map(FileAnalysis::distances).map(vector -> vector.Get(2)).reduce(Scalar::add).orElseThrow();

        /** min and max time */
        LocalDateTime minTime = StaticHelper.getMinVal(filesAnalysis, FileAnalysis::getMinTime);
        LocalDateTime maxTime = StaticHelper.getMaxVal(filesAnalysis, FileAnalysis::getMaxTime);

        /** {minLat,maxLat,minLng,maxLng} */
        Double minLat = StaticHelper.getMinVal(filesAnalysis, FileAnalysis::getMinLat);
        Double maxLat = StaticHelper.getMaxVal(filesAnalysis, FileAnalysis::getMaxLat);

        /** min and max lng */
        Double minLng = StaticHelper.getMinVal(filesAnalysis, FileAnalysis::getMinLng);
        Double maxLng = StaticHelper.getMaxVal(filesAnalysis, FileAnalysis::getMaxLng);

        // /** journey Times */
        // Tensor journeyTimes = Tensors.empty();
        // for (FileAnalysis fileAnalysis : filesAnalysis) {
        // if (Objects.nonNull(fileAnalysis.getJourneyTimes()))
        // fileAnalysis.getJourneyTimes().flatten(-1).forEach(s -> journeyTimes.append(s));
        // }
        // GlobalAssert.that(journeyTimes.length() == numRequests);

        // /** plot waiting Times */
        // Tensor plotWaitingTimes = Tensors.empty();
        // for (FileAnalysis fileAnalysis : filesAnalysis) {
        // if (Objects.nonNull(fileAnalysis.getPlotWaitingTimes()))
        // fileAnalysis.getPlotWaitingTimes().stream().forEach(s -> plotWaitingTimes.append(s));
        // }

        // GlobalAssert.that(maxWaitingTimes.length() == numRequests);
        // GlobalAssert.that(minWaitingTimes.length() == numRequests);

        /** min and max journeyTime */
        Integer minJourneyTime = StaticHelper.getMinVal(filesAnalysis, FileAnalysis::getMinJourneyTime);
        Integer maxJourneyTime = StaticHelper.getMaxVal(filesAnalysis, FileAnalysis::getMaxJourneyTime);

        /** printout general */
        out.write("requests: " + numRequests + "\n");

        if (Objects.nonNull(minTime)) {
            out.write("min Time:\n");
            out.write(minTime.toString());
        }
        if (Objects.nonNull(maxTime)) {
            out.write("max Time:\n");
            out.write(maxTime.toString());
        }
        out.write("lattitude in range: (" + minLat + " / " + maxLat + ")\n");
        out.write("longitude in range: (" + minLng + " / " + maxLng + ")\n");
        out.write("min journey time: " + minJourneyTime + "\n");
        out.write("max journey time: " + maxJourneyTime + "\n");
        // if (journeyTimes.length() > 0) {
        // out.write("mean journey time: " + N.DOUBLE.of(Mean.of(journeyTimes)) + "\n");
        // } else {
        // out.write("mean journey time: " + "no journeys found" + "\n");
        // }

        out.write("total distance: " + totalDistance + "\n");
        out.write("customer distance: " + custrDistance + "\n");
        out.write("empty distance: " + emptyDistance + "\n");

        /** printout per file */
        out.write("=========================\n");
        for (FileAnalysis fileAnalysis : filesAnalysis)
            SaveInfo.ofSingle(fileAnalysis, out, timeConvert);

        // /** save data */
        // if (journeyTimes.length() > 0)
        // UnitSaveUtils.saveFile(journeyTimes, "journeyTimes", saveSubDir);
        // if (plotWaitingTimes.length() > 0)
        // UnitSaveUtils.saveFile(plotWaitingTimes, "plotWaitingTimes", saveSubDir);

    }

    private static void ofSingle(FileAnalysis fileAnalysis, BufferedWriter out, AmodeusTimeConvert timeConvert) throws Exception {
        out.write(fileAnalysis.getFileName() + "\n");
        out.write("requests: " + fileAnalysis.getNumRequests() + "\n");
        out.write("min Time:\n");
        if (Objects.nonNull(fileAnalysis.getMinTime()))
            out.write(fileAnalysis.getMinTime().toString());
        else
            out.write("no times found");
        out.write("max Time:\n");
        if (Objects.nonNull(fileAnalysis.getMaxTime()))
            out.write(fileAnalysis.getMaxTime().toString());
        else
            out.write("no times found");
        out.write("lattitude in range: (" + fileAnalysis.getMinLat() + " / " + fileAnalysis.getMaxLat() + ")\n");
        out.write("longitude in range: (" + fileAnalysis.getMinLng() + " / " + fileAnalysis.getMaxLng() + ")\n");
        out.write("min journey time: " + fileAnalysis.getMinJourneyTime() + "\n");
        out.write("max journey time: " + fileAnalysis.getMaxJourneyTime() + "\n");

        // if (Objects.nonNull(fileAnalysis.getJourneyTimes()) && fileAnalysis.getJourneyTimes().length() > 0//
        // && Scalars.lessThan(Quantity.of(0, SI.SECOND), (Scalar) Total.of(fileAnalysis.getJourneyTimes()))) {
        // out.write("mean journey time: " + N.DOUBLE.of(Mean.of(fileAnalysis.getJourneyTimes())) + "\n");
        // } else {
        // out.write("mean journey time: undefined" + "\n");
        // }

        out.write("empty distance:    " + fileAnalysis.distances().Get(0) + "\n");
        out.write("customer distance: " + fileAnalysis.distances().Get(1) + "\n");
        out.write("total distance:    " + fileAnalysis.distances().Get(2) + "\n");

        Map<LocalDate, Tensor> dateSplitUp = fileAnalysis.getDateSplitUp(); // TODO Claudio
        for (LocalDate localDate : dateSplitUp.keySet()) {
            out.write(localDate + ":  from " + dateSplitUp.get(localDate).Get(0) + " to " + dateSplitUp.get(localDate).Get(0) + "\n");
        }
        out.write("=======\n");
    }
}
