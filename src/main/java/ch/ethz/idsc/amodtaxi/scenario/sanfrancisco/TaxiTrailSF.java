/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripFinder;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;
import ch.ethz.idsc.amodtaxi.trace.TaxiTrail;

/* package */ class TaxiTrailSF implements TaxiTrail {

    private final String id;
    protected int override = 0;
    protected final NavigableMap<LocalDateTime, TaxiStamp> timeTaxiStamps = new TreeMap<>();
    // private final SFTrailProcess sfTrailProcess;// = new SFTrailProcess();
    private final AmodeusTimeConvert timeConvert;
    public final RequestInserter requestInserter;
    private Collection<TaxiTrip> taxiTrips;

    public TaxiTrailSF(String id, FastLinkLookup fastLinkLookup) {
        this.id = id;
        timeConvert = new AmodeusTimeConvert(ZoneId.of("America/Los_Angeles"));
        // sfTrailProcess = new SFTrailProcess(timeConvert, db, qt, id);
        requestInserter = new RequestInserter(timeConvert, fastLinkLookup, id);
    }

    /** adding addtional TaxiStamp */
    @Override
    public void insert(List<String> list) {
        TaxiStamp taxiStamp = TaxiStampConvertedSF.INSTANCE.from(list, timeConvert);
        if (timeTaxiStamps.containsKey(taxiStamp.globalTime)) {
            GlobalAssert.that(false);
            System.err.println("override");
            ++override;
            System.out.println("override: " + override);
        }
        timeTaxiStamps.put(taxiStamp.globalTime, taxiStamp);
    }

    /** method assumes that all {@link TaxiStamp}s are inserted and post processes
     * the data to have all necessary files to create the simulation objects
     * including {@link RoboTaxiStatus} and {@link RequestStatus}
     * 
     * @throws Exception */
    @Override
    public void processFilledTrail() throws Exception {
        SFTrailProcess.basicRoboTaxiStatusProcess(timeTaxiStamps);
        taxiTrips = TaxiTripFinder.in(timeTaxiStamps, id);
        requestInserter.insert(timeTaxiStamps, taxiTrips);
    }

    @Override
    public LocalDateTime getMaxTime() {
        return timeTaxiStamps.lastKey();
    }

    public RequestInserter getRequestInseter() {
        return requestInserter;
    }

    @Override
    public NavigableMap<LocalDateTime, TaxiStamp> getTaxiStamps() {
        return timeTaxiStamps;
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public Collection<TaxiTrip> allTripsBeginningOn(LocalDate localDate) {
        final LocalDateTime endOfDay = localDate.atTime(23, 59, 59);
        // return taxiTrips.stream()//
        //         .filter(t -> LocalDateTimes.lessEquals(localDate.atStartOfDay(), t.pickupTimeDate)) //
        //         .filter(t -> LocalDateTimes.lessEquals(t.pickupTimeDate, endOfDay)) //
        //         .collect(Collectors.toList());
        return taxiTrips.stream() //
                .filter(t -> localDate.atStartOfDay().isEqual(t.pickupTimeDate) || localDate.atStartOfDay().isBefore(t.pickupTimeDate)) //
                .filter(t -> endOfDay.isEqual(t.pickupTimeDate) || endOfDay.isAfter(t.pickupTimeDate)).collect(Collectors.toList()); // TODO why not < 24:00?
    }
}
