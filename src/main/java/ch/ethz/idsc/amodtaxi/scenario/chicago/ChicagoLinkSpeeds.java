/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.chicago;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedUtils;
import ch.ethz.idsc.amodtaxi.scenario.ScenarioLabels;
import org.matsim.api.core.v01.network.Network;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.options.ScenarioOptionsBase;
import ch.ethz.idsc.amodeus.taxitrip.ImportTaxiTrips;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.linkspeed.TaxiLinkSpeedEstimator;
import ch.ethz.idsc.amodtaxi.linkspeed.batch.FlowTimeInvLinkSpeed;
import ch.ethz.idsc.amodtaxi.linkspeed.batch.GLPKLinOptDelayCalculator;

/* package */ enum ChicagoLinkSpeeds {
    ;

    public static void compute(File processingDir, File finalTripsFile) throws Exception {
        File linkSpeedsFile = new File(processingDir, ScenarioLabels.linkSpeedData);

        // load necessary files
        ScenarioOptions scenarioOptions = new ScenarioOptions(processingDir, //
                ScenarioOptionsBase.getDefault());
        File configFile = new File(scenarioOptions.getPreparerConfigName());
        System.out.println(configFile.getAbsolutePath());
        GlobalAssert.that(configFile.exists());
        Config configFull = ConfigUtils.loadConfig(configFile.toString());
        Network network = NetworkLoader.fromNetworkFile(new File(processingDir, configFull.network().getInputFile()));
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, scenarioOptions.getLocationSpec().referenceFrame());

        // import final trips
        Collection<TaxiTrip> trips = new ArrayList<>(ImportTaxiTrips.fromFile(finalTripsFile));

        // export link speed estimation
        // QuadTree<Link> qt = FastQuadTree.of(network);
        TaxiLinkSpeedEstimator lsCalc = new FlowTimeInvLinkSpeed(trips, network, db, GLPKLinOptDelayCalculator.INSTANCE);
        LinkSpeedUtils.writeLinkSpeedData(linkSpeedsFile, lsCalc.getLsData());
    }
}
