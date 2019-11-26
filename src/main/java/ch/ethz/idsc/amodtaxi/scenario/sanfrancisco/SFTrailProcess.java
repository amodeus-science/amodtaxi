/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDateTime;
import java.util.NavigableMap;

import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;

/* package */ enum SFTrailProcess {
    ;

    // public final RequestInserter requestInserter;
    //
    // public SFTrailProcess(AmodeusTimeConvert timeConvert, MatsimAmodeusDatabase db, QuadTree<Link> qt, //
    // String taxiId) {
    // this.requestInserter = new RequestInserter(timeConvert, db, qt, taxiId);
    // }
    //
    // // TODO reimplement more sophisticated methods (basic version wo details)
    // public void process(NavigableMap<LocalDateTime, TaxiStamp> timeTaxiStamps) throws Exception {
    // basicRoboTaxiStatusProcess(timeTaxiStamps);
    // basicRoboTaxiRequestProcess(timeTaxiStamps);
    // }

    /** based on the @param timeTaxiStamps fill the {@link RoboTaxiStatus} in the TaxiStamps:DRIVINGWITHCUSTOMER if
     * somebody on board, STAY otherwise** */
    public static void basicRoboTaxiStatusProcess(NavigableMap<LocalDateTime, TaxiStamp> timeTaxiStamps) {
        for (TaxiStamp taxiStamp : timeTaxiStamps.values()) {
            if (taxiStamp.occupied == true) {
                taxiStamp.roboTaxiStatus = RoboTaxiStatus.DRIVEWITHCUSTOMER;
            } else {
                taxiStamp.roboTaxiStatus = RoboTaxiStatus.STAY;
            }
        }
    }

    // private void basicRoboTaxiRequestProcess(NavigableMap<LocalDateTime, TaxiStamp> timeTaxiStamps)//
    // throws Exception {
    // /** find all requests in trail */
    // requestInserter.insert(timeTaxiStamps);
    // }
}
