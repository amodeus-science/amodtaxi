package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.api.core.v01.population.Population;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.TaxiStamp;

public enum Consistency {
    ;

    public static void checkTrail(List<TaxiTrail> trails) {
        /** are all values recorded properly */
        for (TaxiTrail taxiTrailSF : trails) {
            for (TaxiStamp taxiStamp : taxiTrailSF.getTaxiStamps().values()) {
                GlobalAssert.that(Objects.nonNull(taxiStamp.roboTaxiStatus));
                GlobalAssert.that(Objects.nonNull(taxiStamp.gps));
            }
        }

        /** any step with RoboTaxiStauts==DWC must have a requeststatus driving */
        for (TaxiTrail taxiTrail : trails) {
            for (@SuppressWarnings("unused")
            LocalDateTime time : taxiTrail.getTaxiStamps().keySet()) {
                // TODO
                // ---
            }
        }
    }

    public static void check(Population population) {
        /** leg departure times */
        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                for (PlanElement pElem : plan.getPlanElements()) {
                    if (pElem instanceof Leg) {
                        Leg leg = (Leg) pElem;
                        GlobalAssert.that(leg.getDepartureTime() >= 0);
                    }
                }
            }
        }
    }

}
