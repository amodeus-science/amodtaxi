package ch.ethz.idsc.amodtaxi.linkspeed.research;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.linkspeed.LinkIndex;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodtaxi.linkspeed.LinkSpeedsExport;
import ch.ethz.idsc.amodtaxi.scenario.chicago.ChicagoReferenceFrames;

/* package */ class CreateTrafficProfile {

    public static void main(String[] args) throws IOException {

        /** initial data */
        ReferenceFrame referenceFrame = ChicagoReferenceFrames.CHICAGO;
        File networkFile = new File("/home/clruch/data/TestDeleteMe/network.xml.gz");
        Random random = new Random(10);
        Network network = NetworkLoader.fromNetworkFile(networkFile);
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);
        int dt = 450;
        double reduction = 0.5;
        int maxNumberBadLinks = 100;

        /** select some bad links */
        int bound = network.getLinks().size();
        Set<Link> badLinks = new HashSet();
        for (int i = 0; i < maxNumberBadLinks; ++i) {
            int elemRand = random.nextInt(bound);
            Link link = network.getLinks().values().stream().skip(elemRand).findFirst().get();
            badLinks.add(link);
        }

        /** reduce all bad links according to reduction */
        LinkSpeedDataContainer lsData = new LinkSpeedDataContainer();
        for (Link link : badLinks) {
            for (int time = 0; time <= 108000; time += dt) {
                Integer linkId = LinkIndex.fromLink(db, link);
                double freeSpeed = link.getFreespeed();
                lsData.addData(linkId, time, freeSpeed * reduction);
            }
        }

        /** save the traffic profile to the file system */
        File linkSpeedsFile = new File("/home/clruch/data/TestDeleteMe/randomTraffic");
        LinkSpeedsExport.using(linkSpeedsFile, lsData);

    }

}
