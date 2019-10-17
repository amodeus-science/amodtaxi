/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;

import org.matsim.api.core.v01.Coord;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.LocalDateTimes;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;

/* package */ class RequestInserter {

    private final AmodeusTimeConvert timeConvert;
    private final MatsimAmodeusDatabase db;
    private final FastLinkLookup qt;
    // private final String taxiId;
    private Map<TaxiStamp, Collection<RequestContainer>> reqContainers = new HashMap<>();

    public RequestInserter(AmodeusTimeConvert timeConvert, MatsimAmodeusDatabase db, //
            FastLinkLookup qt, String taxiId) {
        this.timeConvert = timeConvert;
        this.db = db;
        this.qt = qt;
        // TODO taxi id is not used
        // this.taxiId = taxiId;
    }

    /** Function computes all required {@link RequestContainer}s from the inserted
     * map @param timeTaxiStamps. Every request belongs to the date of its submission, even
     * if its arrival is on the next day!
     * 
     * @throws Exception */
    public void insert(NavigableMap<LocalDateTime, TaxiStamp> timeTaxiStamps, //
            Collection<TaxiTrip> taxiTrips) throws Exception {
        // Collection<TaxiTrip> taxiTrips = TaxiTripFinder.in(timeTaxiStamps, taxiId);
        System.err.println("found " + taxiTrips.size() + " taxi trips.");

        for (TaxiTrip taxiTrip : taxiTrips) {

            Map<RequestStatus, LocalDateTime> reqTimes = //
                    StaticHelper1.getRequestTimes(taxiTrip.pickupDate, timeTaxiStamps);

            /** basic setup of RequestContainer */
            LocalDateTime submissionTime = reqTimes.get(RequestStatus.REQUESTED);
            if (Objects.isNull(submissionTime))
                submissionTime = reqTimes.get(RequestStatus.ASSIGNED);
            if (Objects.isNull(submissionTime))
                submissionTime = reqTimes.get(RequestStatus.PICKUPDRIVE);
            if (Objects.isNull(submissionTime))
                submissionTime = reqTimes.get(RequestStatus.PICKUP);
            if (Objects.isNull(submissionTime))
                submissionTime = reqTimes.get(RequestStatus.DRIVING);
            LocalDate submissionDay = submissionTime.toLocalDate();
            Objects.requireNonNull(submissionDay);

            /** from link */
            Coord position = db.referenceFrame.coords_fromWGS84().transform(timeTaxiStamps.get(taxiTrip.pickupDate).gps);
            int fromLinkIndex = qt.getLinkIndexFromXY(position);

            /** to link */
            LocalDateTime lastDriveTimeSTep = timeTaxiStamps.lowerKey(taxiTrip.dropoffDate);
            Coord positionEnd = db.referenceFrame.coords_fromWGS84().transform(timeTaxiStamps.get(lastDriveTimeSTep).gps);
            int toLinkIndex = qt.getLinkIndexFromXY(positionEnd);

            RequestContainerFactory rcf = new RequestContainerFactory(//
                    taxiTrip.localId, fromLinkIndex, toLinkIndex, //
                    submissionTime, timeConvert);

            /** add request containers before driving */
            Collection<RequestStatus> statii = new ArrayList<>();
            statii.add(RequestStatus.REQUESTED);
            statii.add(RequestStatus.ASSIGNED);
            statii.add(RequestStatus.PICKUPDRIVE);
            statii.add(RequestStatus.PICKUP);
            for (RequestStatus status : statii) {
                if (Objects.nonNull(reqTimes.get(status))) {
                    RequestContainer container = rcf.create(status, submissionDay);
                    addContainer(timeTaxiStamps.get(reqTimes.get(status)), container);
                }
            }

            /** add request containers while driving */
            LocalDateTime time = taxiTrip.pickupDate;
            do {
                RequestContainer container = rcf.create(RequestStatus.DRIVING, submissionDay);
                addContainer(timeTaxiStamps.get(time), container);
                time = timeTaxiStamps.higherKey(time);
            } while (Objects.nonNull(time) && LocalDateTimes.lessEquals(time, taxiTrip.dropoffDate));

            /** add request containers after driving */
            RequestContainer container = rcf.create(RequestStatus.DROPOFF, submissionDay);
            addContainer(timeTaxiStamps.get(taxiTrip.dropoffDate), container);
        }

        /** One {@link TaxiTrip} will produce several {@link RequestContainer} that are
         * saved in a {@link SimulationObject}, therefore if the below condition is
         * not met there must be a problem in the computation. */
        GlobalAssert.that(reqContainers.size() >= taxiTrips.size());

    }

    private void addContainer(TaxiStamp stamp, RequestContainer container) {
        if (reqContainers.containsKey(stamp)) {
            reqContainers.get(stamp).add(container);
        } else {
            reqContainers.put(stamp, new ArrayList<>());
            reqContainers.get(stamp).add(container);
        }
    }

    public Collection<RequestContainer> getReqContainerCopy(TaxiStamp taxiStamp) {
        if (!reqContainers.containsKey(taxiStamp)) {
            return null;
        }
        Collection<RequestContainer> reqContainer = reqContainers.get(taxiStamp);
        Collection<RequestContainer> colCopy = new ArrayList<>();
        for (RequestContainer rc : reqContainer) {
            RequestContainer copy = new RequestContainer();
            copy.fromLinkIndex = rc.fromLinkIndex;
            copy.toLinkIndex = rc.toLinkIndex;
            copy.submissionTime = rc.submissionTime;
            copy.requestIndex = rc.requestIndex;
            copy.requestStatus = rc.requestStatus;
            colCopy.add(copy);
        }
        return colCopy;
    }
}
