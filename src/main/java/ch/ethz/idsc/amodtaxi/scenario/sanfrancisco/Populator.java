/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDate;
import java.util.Collection;

import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationFactory;

import ch.ethz.idsc.amodeus.taxitrip.PersonCreate;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.geo.ClosestLinkSelect;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

/* package */ class Populator {

    private long globalId = 0;
    private final Population population;
    private final AmodeusTimeConvert timeConvert;
    private final ClosestLinkSelect linkSelect;
    private final PopulationFactory populationFactory;
    private final LocalDate simulationDate;

    public Populator(Population population, AmodeusTimeConvert timeConvert, //
            ClosestLinkSelect linkSelect, LocalDate simulationDate) {//
        this.population = population;
        this.timeConvert = timeConvert;
        this.linkSelect = linkSelect;
        populationFactory = population.getFactory();
        this.simulationDate = simulationDate;
    }

    public void convert(Collection<TaxiTrip> taxiTrips) {
        /** insert all {@link TaxiTrip}s into {@link Population} */
        taxiTrips.stream()//
                .forEach(taxiTrip -> {
                    Person person = PersonCreate.fromTrip(taxiTrip, ++globalId, populationFactory, //
                            linkSelect, simulationDate, timeConvert);
                    population.addPerson(person);
                });
        GlobalAssert.that(population.getPersons().size() == taxiTrips.size());
    }

}
