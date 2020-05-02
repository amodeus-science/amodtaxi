/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.fleetconvert;

import java.io.File;
import java.util.Collection;
import java.util.Collections;

import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsSupplier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.matsim.api.core.v01.network.Network;

import ch.ethz.idsc.amodeus.options.ScenarioOptions;
import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.scenario.TaxiTripsReader;
import ch.ethz.idsc.amodtaxi.tripfilter.TaxiTripFilterCollection;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifier;

public abstract class ReaderTripFleetConverter extends TripFleetConverter {
    public ReaderTripFleetConverter(ScenarioOptions scenarioOptions, Network network, //
            TaxiDataModifier tripModifier, //
            TaxiDataModifier generalModifier, TaxiTripFilterCollection finalFilters, //
            TaxiTripsReader tripsReader, File tripFile, File targetDirectory) {
        super(scenarioOptions, network, tripModifier, finalFilters, createSupplier(tripFile, targetDirectory, tripsReader, generalModifier), targetDirectory);
    }

    private static TaxiTripsSupplier createSupplier(File tripFile, File targetDirectory, TaxiTripsReader tripsReader, TaxiDataModifier modifier) {
        return new TaxiTripsSupplier() {
            @Override
            public Collection<TaxiTrip> get() {
                try {
                    /** folder for processing stored files, the folder tripData contains
                     * .csv versions of all processing steps for faster debugging. */
                    FileUtils.copyFileToDirectory(tripFile, targetDirectory);
                    File newTripFile = new File(targetDirectory, tripFile.getName());
                    System.out.println("NewTripFile: " + newTripFile.getAbsolutePath());
                    GlobalAssert.that(newTripFile.isFile());

                    /** initial formal modifications, e.g., replacing certain characters,
                     * other modifications should be done in the third step */
                    File preparedFile = modifier.modify(newTripFile);

                    /** save unreadable trips for post-processing, checking */
                    File unreadable = new File(preparedFile.getParentFile(), //
                            FilenameUtils.getBaseName(preparedFile.getAbsolutePath()) + "_unreadable." + //
                                    FilenameUtils.getExtension(preparedFile.getAbsolutePath()));
                    tripsReader.saveUnreadable(unreadable);

                    return tripsReader.getTrips(preparedFile);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Collections.emptyList();
                }
            }
        };
    }
}
