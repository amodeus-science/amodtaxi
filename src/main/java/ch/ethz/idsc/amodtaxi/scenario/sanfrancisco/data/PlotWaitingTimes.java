/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.data;

import java.time.LocalDateTime;
import java.util.SortedMap;

import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

/* package */ enum PlotWaitingTimes {
    ;

    // TODO check this function and modify if necessary
    public static Tensor in(SortedMap<LocalDateTime, TaxiStamp> sortedEntries) throws Exception {
        Tensor plotWaitingTimes = Tensors.empty();
        // double prevLat = sortedEntries.get(sortedEntries.firstKey()).gps.getY();
        // double prevLon = sortedEntries.get(sortedEntries.firstKey()).gps.getX();
        // Coord prevCoord = new Coord(prevLon, prevLat);
        // LocalDateTime prevDataTime = sortedEntries.get(sortedEntries.firstKey()).globalTime;
        // LocalDateTime dataTime = null;
        //
        // for (LocalDateTime time : sortedEntries.keySet()) {
        // double lat = sortedEntries.get(time).gps.getY();
        // double lon = sortedEntries.get(time).gps.getX();
        // Coord coord = new Coord(lon, lat);
        // dataTime = sortedEntries.get(time).globalTime;
        //
        // Scalar timeIncrement = Duration.between(prevDataTime, dataTime);// (dataTime - prevDataTime);
        //
        // ReferenceFrame referenceFrame = ReferenceFrames.SANFRANCISCO;
        //
        // Coord local1 = referenceFrame.coords_fromWGS84().transform(coord);
        // Coord local2 = referenceFrame.coords_fromWGS84().transform(prevCoord);
        //
        // double dist = CoordUtils.calcEuclideanDistance(local1, local2);
        //
        // Tensor row = Tensors.empty();
        // row.append(timeIncrement);
        // row.append(RealScalar.of(dist));
        // plotWaitingTimes.append(row);
        //
        // prevLat = sortedEntries.get(time).gps.getY();
        // prevLon = sortedEntries.get(time).gps.getX();
        // prevDataTime = sortedEntries.get(time).globalTime;
        // prevCoord = new Coord(prevLon, prevLat);
        // }
        //
        return plotWaitingTimes;
    }
}
