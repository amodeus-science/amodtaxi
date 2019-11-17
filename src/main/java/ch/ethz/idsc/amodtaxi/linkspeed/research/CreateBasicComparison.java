package ch.ethz.idsc.amodtaxi.linkspeed.research;

import java.io.File;
import java.util.Objects;
import java.util.Random;

import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.data.ReferenceFrame;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedDataContainer;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedTimeSeries;
import ch.ethz.idsc.amodeus.linkspeed.LinkSpeedUtils;
import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodtaxi.scenario.chicago.ChicagoReferenceFrames;
import ch.ethz.idsc.tensor.Tensor;
import ch.ethz.idsc.tensor.Tensors;
import ch.ethz.idsc.tensor.io.Export;
import ch.ethz.idsc.tensor.io.Pretty;

public class CreateBasicComparison {

    public static void main(String[] args) throws Exception {

        ReferenceFrame referenceFrame = ChicagoReferenceFrames.CHICAGO;
        File networkFile = new File("/home/oem/data/ChicagoScenario/network.xml.gz");
        Random random = new Random(10);
        Network network = NetworkLoader.fromNetworkFile(networkFile);
        MatsimAmodeusDatabase db = MatsimAmodeusDatabase.initialize(network, referenceFrame);

        // load the known random traffic
        File randomTrafifFile = new File("/home/oem/data/chicagotest/randomTraffic");
        LinkSpeedDataContainer lsDataRandom = LinkSpeedUtils.loadLinkSpeedData(randomTrafifFile);

        // load the algorithm traffic
        File algorithmTraffic = new File("/home/oem/data/chicagotest/linkSpeedData");
        LinkSpeedDataContainer lsDataAlgorit = LinkSpeedUtils.loadLinkSpeedData(algorithmTraffic);

        Tensor differences = Tensors.empty();

        for (Integer linkID : lsDataRandom.getLinkMap().keySet()) {
            LinkSpeedTimeSeries seriesTraffic = lsDataRandom.getLinkMap().get(linkID);
            Link link = db.getOsmLink(linkID).link;

            for (Integer time : seriesTraffic.getRecordedTimes()) {
                double trafficSpeed = seriesTraffic.getSpeedsAt(time);

                LinkSpeedTimeSeries seriesAlgo = lsDataAlgorit.getLinkMap().get(linkID);
                if (Objects.nonNull(seriesAlgo)) {
                    Double speedAlgo = seriesAlgo.getSpeedsAt(time);
                    if (Objects.nonNull(speedAlgo)) {
                        differences.append(Tensors.vector(linkID, time, trafficSpeed, speedAlgo, link.getFreespeed()));
                    } else {
                        // TODO what if link is covered but not time?
                        differences.append(Tensors.vector(linkID, time, trafficSpeed, 0, link.getFreespeed()));
                    }
                } else {
                    differences.append(Tensors.vector(linkID, time, trafficSpeed, 1, link.getFreespeed()));
                }
            }
        }

        // loop for links which are changed in speedsAlgo but n ot in the generated traffic speed
        // for (Integer linkID : lsDataAlgorit.getLinkMap().keySet()) {
        // LinkSpeedTimeSeries seriesAlgo = lsDataAlgorit.getLinkMap().get(linkID);
        // Link link = db.getOsmLink(linkID).link;
        //
        // for (Integer time : seriesAlgo.getRecordedTimes()) {
        // double speedAlgo = seriesAlgo.getSpeedsAt(time);
        //
        // LinkSpeedTimeSeries seriesTraffic = lsDataRandom.getLinkMap().get(linkID);
        // if (!Objects.nonNull(seriesTraffic)) {
        // differences.append(Tensors.vector(linkID, time, 0, speedAlgo, link.getFreespeed()));
        // System.out.println("link only in algo " + speedAlgo);
        // } else {
        // Double speedTraffic = seriesTraffic.getSpeedsAt(time);
        // if (!Objects.nonNull(speedTraffic)) {
        // differences.append(Tensors.vector(linkID, time, 0, speedAlgo, link.getFreespeed()));
        // System.out.println("time only in algo " + speedAlgo);
        // }
        //
        // }
        // }
        // }

        // FIXME the links which are changed in speedsAlgo but not in the generated trafffic speed
        // are not yet printed.

        System.out.println("differences: ");
        System.out.println(Pretty.of(differences));

        Export.of(new File("/home/oem/data/chicagotest/differences.csv"), differences);

    }
}
