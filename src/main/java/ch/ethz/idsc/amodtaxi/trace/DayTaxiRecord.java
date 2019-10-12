package ch.ethz.idsc.amodtaxi.trace;

import java.time.LocalDateTime;
import java.util.List;

import ch.ethz.idsc.amodtaxi.scenario.sanfrancisco.TaxiTrail;

/** A {@link DayTaxiRecord} contains the {@link TaxiTrail} of all taxis in the dataset, first
 * fill with data using the insert function, then postprocess. Every trail is postprocessed independently.
 * 
 * @author clruch */
public interface DayTaxiRecord {

    /** part 1: filling with data */
    /** @param list timestamp in data */
    void insert(List<String> list, int taxiNumber, String id);

    /** part 2: postprocess trails once filled */
    void processFilledTrails() throws Exception;

    /** part 3: access functions */
    TaxiTrail get(int vehicleIndex);

    int numTaxis();

    /** @return oldest {@link LocalDateTime} in all {@link TaxiTrail}s of the {@link DayTaxiRecord} */
    LocalDateTime getMaxTime();

    List<TaxiTrail> getTrails();
}
