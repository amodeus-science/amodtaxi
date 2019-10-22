package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.io.IOException;

import org.matsim.api.core.v01.network.Network;
import org.matsim.core.network.algorithms.NetworkCleaner;
import org.matsim.core.network.io.NetworkWriter;

import ch.ethz.idsc.amodeus.matsim.NetworkLoader;
import ch.ethz.idsc.amodeus.util.io.GZHandler;
import ch.ethz.idsc.amodeus.util.network.LinkModes;

public enum InitialNetworkPreparerSF {
    ;

    /** @param procDir directory in which the network generated with pt2Matsim is
     * @return */
    public static Network run(File procDir) {

        /** load the pt2matsim network */
        Network ntwrkpt2Matsim = NetworkLoader.fromNetworkFile(new File(procDir, "network_pt2matsim.xml"));

        /** remove links on which cars cannot drive */
        LinkModes lnkMds = new LinkModes("car");
        Network fltrdNtwrk = NetworkCutterUtils.modeFilter(ntwrkpt2Matsim, lnkMds);

        /** cleanup the network */
        new NetworkCleaner().run(fltrdNtwrk);

        /** save the network */
        final File fileExpGz = new File(procDir + "/network.xml.gz");
        final File fileExport = new File(procDir + "/network.xml");
        NetworkWriter nw = new NetworkWriter(fltrdNtwrk);
        nw.write(fileExpGz.toString());

        /** extract gz file */
        try {
            GZHandler.extract(fileExpGz, fileExport);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fltrdNtwrk;

    }

}
