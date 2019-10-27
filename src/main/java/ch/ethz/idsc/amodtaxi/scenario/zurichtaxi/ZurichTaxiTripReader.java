package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi;

import java.text.ParseException;
import java.time.LocalDateTime;

import ch.ethz.idsc.amodeus.util.io.CsvReader.Row;
import ch.ethz.idsc.amodtaxi.readers.TaxiTripsReader;
import ch.ethz.idsc.tensor.Scalar;
import ch.ethz.idsc.tensor.Tensor;

public class ZurichTaxiTripReader extends TaxiTripsReader {

    public ZurichTaxiTripReader(String delim) {
        super(delim);
    }

    @Override
    public String getTaxiCode(Row row) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LocalDateTime getStartTime(Row row) throws ParseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LocalDateTime getEndTime(Row row) throws ParseException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Tensor getPickupLocation(Row row) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Tensor getDropoffLocation(Row row) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Scalar getDuration(Row row) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Scalar getDistance(Row row) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Scalar getWaitingTime(Row row) {
        // TODO Auto-generated method stub
        return null;
    }

}
