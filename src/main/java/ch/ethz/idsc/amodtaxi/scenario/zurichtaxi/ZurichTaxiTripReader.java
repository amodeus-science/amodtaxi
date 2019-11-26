/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import static ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.ZurichTaxiConstants.callCenterDateFormat;

import java.text.ParseException;
import java.time.LocalDateTime;

import ch.ethz.idsc.amodeus.util.Duration;
import ch.ethz.idsc.amodeus.util.io.CsvReader.Row;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsReader;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;

public class ZurichTaxiTripReader extends TaxiTripsReader {

    public ZurichTaxiTripReader(String delim) {
        super(delim);
    }

    @Override
    public String getTripId(Row row) {
        return row.get("Id");
    }

    @Override
    public String getTaxiId(Row row) {
        return row.get("Kenng");
    }

    @Override
    public LocalDateTime getSubmissionTime(Row row) throws ParseException {
        return LocalDateTime.parse(row.get("Erfassung"), callCenterDateFormat);
    }

    @Override
    public LocalDateTime getPickupTime(Row row) throws ParseException {
        return LocalDateTime.parse(row.get("Fahrtbeginn"), callCenterDateFormat);
    }

    @Override
    public LocalDateTime getDropoffTime(Row row) throws ParseException {
        return LocalDateTime.parse(row.get("Fahrtende"), callCenterDateFormat);
    }

    @Override
    public Tensor getPickupLocation(Row row) {
        return Tensors.fromString(row.get("WGS84OSMStart").replace(";", ","));
    }

    @Override
    public Tensor getDropoffLocation(Row row) {
        return Tensors.fromString(row.get("WGS84OSMEnd").replace(";", ","));
    }

    @Override
    public Scalar getDuration(Row row) {
        LocalDateTime pickup = LocalDateTime.parse(row.get("Fahrtbeginn"), callCenterDateFormat);
        LocalDateTime dropoff = LocalDateTime.parse(row.get("Fahrtende"), callCenterDateFormat);
        try {
            return Duration.between(pickup, dropoff);
        } catch (Exception e) {
            GlobalAssert.that(false);
            return null;
        }
    }

    @Override
    public Scalar getDistance(Row row) {
        return null;
    }

    @Override
    public Scalar getWaitingTime(Row row) {
        LocalDateTime submission = LocalDateTime.parse(row.get("Erfassung"), callCenterDateFormat);
        LocalDateTime pickup = LocalDateTime.parse(row.get("Fahrtbeginn"), callCenterDateFormat);
        try {
            return Duration.between(submission, pickup);
        } catch (Exception e) {
            GlobalAssert.that(false);
            return null;
        }
    }

}
