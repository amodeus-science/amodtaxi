package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

import ch.ethz.idsc.amodeus.taxitrip.TaxiTrip;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;
import ch.ethz.idsc.amodtaxi.tripmodif.NullModifier;
import ch.ethz.idsc.amodtaxi.tripmodif.TaxiDataModifier;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

public class TaxiTripsSuppliers {
    public static TaxiTripsSupplier fromReader(File tripFile, File targetDirectory, TaxiTripsReader tripsReader) {
        return fromReader(tripFile, targetDirectory, tripsReader, NullModifier.INSTANCE);
    }

    public static TaxiTripsSupplier fromReader(File tripFile, File targetDirectory, TaxiTripsReader tripsReader, TaxiDataModifier modifier) {
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

    public static TaxiTripsSupplier fromDayTaxiRecord(DayTaxiRecord dayTaxiRecord, LocalDate localDate) {
        return () -> AllTaxiTrips.in(dayTaxiRecord).on(localDate);
    }
}
