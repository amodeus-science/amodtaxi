package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.dispatcher.core.RequestStatus;
import ch.ethz.idsc.amodeus.dispatcher.core.RoboTaxiStatus;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.net.RequestContainer;
import ch.ethz.idsc.amodeus.net.SimulationObject;
import ch.ethz.idsc.amodeus.net.VehicleContainer;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;

public class SimContainerPopulator {

    private final MatsimAmodeusDatabase db;
    private final QuadTree<Link> qt;
    private final AmodeusTimeConvert timeConvert;

    public SimContainerPopulator(MatsimAmodeusDatabase db, QuadTree<Link> qt, AmodeusTimeConvert timeConvert) {
        this.db = db;
        this.qt = qt;
        this.timeConvert = timeConvert;
    }

    public void with(TaxiStamp taxiStamp, int vehicleIndex, //
            SimulationObject simulationObject, //
            GlobalRequestIndex globalReqIndex, //
            LocalDate simulationDate, RequestInserter reqInserter) {

        Objects.requireNonNull(taxiStamp);
        Objects.requireNonNull(reqInserter);

        Coord position = db.referenceFrame.coords_fromWGS84().transform(taxiStamp.gps);
        GlobalAssert.that(Objects.nonNull(position));
        Link center = qt.getClosest(position.getX(), position.getY());
        GlobalAssert.that(Objects.nonNull(center));
        int linkIndex = db.getLinkIndex(center);

        /** initialize and add VehicleContainer */
        VehicleContainer vc = new VehicleContainer();
        vc.vehicleIndex = vehicleIndex;
        vc.linkTrace = new int[] { linkIndex };
        vc.roboTaxiStatus = taxiStamp.roboTaxiStatus;
        vc.destinationLinkIndex = linkIndex; // TODO this is just temporary, need to do properly
        GlobalAssert.that(Objects.nonNull(vc.roboTaxiStatus));
        simulationObject.vehicles.add(vc);

        /** request containers */
        if (Objects.nonNull(reqInserter.getReqContainerCopy(taxiStamp)))
            for (RequestContainer rc : reqInserter.getReqContainerCopy(taxiStamp)) {
                /** ensure that globally every index occurs only once */
                LocalDateTime firstValidSubmissionTime = timeConvert.beginOf(simulationDate);
                if (timeConvert.ldtToAmodeus(firstValidSubmissionTime, simulationDate) <= rc.submissionTime) {

                    // LocalDateTimes.lessEquals(firstValidSubmissionTime, rc.submissionTime)) {
                    Integer globalIndex = globalReqIndex.add(vehicleIndex, rc.requestIndex);
                    RequestContainer copy = RCDeepCopy.deepCopy(rc);
                    // System.out.println("copy submission time: " + copy.submissionTime);
                    // copy.submissionTime = timeConvert.toAmodeus((int) copy.submissionTime, localDate);
                    copy.requestIndex = globalIndex;
                    GlobalAssert.that(copy.submissionTime >= 0);
                    simulationObject.requests.add(copy);
                    if (copy.requestStatus.equals(RequestStatus.PICKUP))
                        simulationObject.total_matchedRequests += 1;
                }
            }
    }

    public void withEmpty(SimulationObject simulationObject, int vehicleIndex) {
        /** place vehicles on arbitrary link */
        VehicleContainer vc = new VehicleContainer();
        vc.vehicleIndex = vehicleIndex;
        vc.linkTrace = new int[] { 1 };
        vc.roboTaxiStatus = RoboTaxiStatus.STAY;
        GlobalAssert.that(Objects.nonNull(vc.roboTaxiStatus));
        simulationObject.vehicles.add(vc);
    }
}
