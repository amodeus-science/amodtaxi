package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;

interface CsvFleetReaderInterface {

    /** @param file a csv file with taxi journey information
     * @param dataDirectory a directory in wich the file is located
     * @param taxiNumber the number of the taxi
     * @return {@link DayTaxiRecord} in which all of the informations in the file are loaded
     * @throws Exception */
    DayTaxiRecord populateFrom(File trail, int taxiNumber) throws Exception;

}
