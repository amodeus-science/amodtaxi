/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.time.LocalDate;
import java.util.Optional;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.fleetconvert.TripFleetConverter;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FilenameUtils;

public class Scenario {

    public static File create(File dataDir, File tripFile, TripFleetConverter converter, //
            File processingDir, LocalDate simulationDate, AmodeusTimeConvert timeConvert) throws Exception {
        Scenario creator = new Scenario(dataDir, tripFile, converter, processingDir, simulationDate, timeConvert);
        creator.run();
        return creator.getFinalTripFile().orElseThrow();
    }

    // ---

    private final File dataDir;
    private final File tripFile;
    private final File processingDir;
    private final LocalDate simulationDate;
    private final AmodeusTimeConvert timeConvert;
    private final TripFleetConverter fleetConverter;

    private Scenario(File dataDir, File tripFile, //
            TripFleetConverter converter, //
            File processingDir, //
            LocalDate simulationDate, //
            AmodeusTimeConvert timeConvert) {
        GlobalAssert.that(dataDir.isDirectory());
        GlobalAssert.that(tripFile.exists());
        this.dataDir = dataDir;
        this.tripFile = tripFile;
        this.processingDir = processingDir;
        this.simulationDate = simulationDate;
        this.timeConvert = timeConvert;
        this.fleetConverter = converter;
    }

    private void run() throws Exception {
        InitialFiles.copyToDir(processingDir, dataDir);
        fleetConverter.setFilters();
        fleetConverter.run(processingDir, FilenameUtils.getBaseName(tripFile.getPath()), FileNameUtils.getExtension(tripFile.getPath()), simulationDate, timeConvert);
    }

    public Optional<File> getFinalTripFile() {
        return fleetConverter.getFinalTripFile();
    }
}
