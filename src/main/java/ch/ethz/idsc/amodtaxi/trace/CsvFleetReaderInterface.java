/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.trace;

import java.io.File;

public interface CsvFleetReaderInterface {

    /** @param file a csv file with taxi journey information
     * @param dataDirectory a directory in wich the file is located
     * @param taxiNumber the number of the taxi
     * @return {@link DayTaxiRecord} in which all of the informations in the file are loaded
     * @throws Exception */
    DayTaxiRecord populateFrom(File trail, int taxiNumber) throws Exception;

}
