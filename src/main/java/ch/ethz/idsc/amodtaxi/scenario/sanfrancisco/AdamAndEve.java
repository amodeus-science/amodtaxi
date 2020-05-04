/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.time.LocalDate;
import java.util.Collection;

import ch.ethz.idsc.amodtaxi.scenario.Consistency;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.groups.PlansConfigGroup;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.utils.collections.QuadTree;

import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.geo.ClosestLinkSelect;
import ch.ethz.idsc.amodeus.util.io.GZHandler;

/* package */ enum AdamAndEve {
    ;

    public static void create(File workingDirectory, Collection<TaxiTrip> trips, Network network, //
            MatsimAmodeusDatabase db, //
            AmodeusTimeConvert timeConvert, QuadTree<Link> quadTree, LocalDate simulationDate, //
            String nameAdd)//
            throws Exception {

        /** create a new {@link Population} */
        Population population = PopulationUtils.createPopulation(new PlansConfigGroup(), network);

        /** fill with {@link TaxiTrip}s */
        ClosestLinkSelect linkSelect = new ClosestLinkSelect(db, quadTree);
        (new Populator(population, timeConvert, linkSelect, simulationDate)).convert(trips);
        Consistency.check(population);

        /** write created {@link Population} to file */
        File populationFile = new File(workingDirectory, "population" + nameAdd + ".xml");
        File populationGzFile = new File(workingDirectory, "population" + nameAdd + ".xml.gz");
        System.out.println("INFO writing new population to:");
        System.out.println(populationFile.getPath());
        System.out.println(populationGzFile.getPath());
        PopulationWriter pw = new PopulationWriter(population);
        pw.write(populationGzFile.toString());

        /** extract .gz file */
        GZHandler.extract(populationGzFile, populationFile);
        System.out.println("INFO successfully created population");
    }

}
