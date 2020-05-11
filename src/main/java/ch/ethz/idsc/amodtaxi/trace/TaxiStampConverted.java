/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.trace;

import java.util.List;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;

@Deprecated
@FunctionalInterface
public interface TaxiStampConverted {
    // TODO doc
    TaxiStamp from(List<String> dataFileRow, AmodeusTimeConvert timeConvert);

}
