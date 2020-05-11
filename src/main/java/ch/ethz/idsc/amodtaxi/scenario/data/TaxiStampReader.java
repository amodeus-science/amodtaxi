package ch.ethz.idsc.amodtaxi.scenario.data;

import java.util.List;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;

public interface TaxiStampReader {
    /** create a {@link TaxiStamp} from
     * @param dataRow {@link List<String>}
     * @return {@link TaxiStamp} */
    TaxiStamp read(List<String> dataRow);

    /** @return {@link AmodeusTimeConvert} used */
    AmodeusTimeConvert timeConvert();
}
