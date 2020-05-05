/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.time.LocalDate;
import java.util.Optional;

import ch.ethz.idsc.amodeus.util.AmodeusTimeConvert;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.fleetconvert.TripFleetConverter;
import ch.ethz.idsc.amodtaxi.util.NamingConvention;

public class Scenario {

    public static File create(File dataDir, File tripFile, TripFleetConverter converter, File processingDir, //
            LocalDate simulationDate, AmodeusTimeConvert timeConvert) throws Exception {
        Scenario creator = new Scenario(dataDir, NamingConvention.similarTo(tripFile), converter, processingDir, simulationDate, timeConvert);
        creator.run();
        return creator.getFinalTripFile().orElseThrow();
    }

    public static File create(File dataDir, TripFleetConverter converter, File processingDir, //
            LocalDate simulationDate, AmodeusTimeConvert timeConvert) throws Exception {
        Scenario creator = new Scenario(dataDir, NamingConvention.using("taxi_trips", "csv"), converter, processingDir, simulationDate, timeConvert);
        creator.run();
        return creator.getFinalTripFile().orElseThrow();
    }

    // ---

    private final File dataDir;
    private final NamingConvention convention;
    private final File processingDir;
    private final LocalDate simulationDate;
    private final AmodeusTimeConvert timeConvert;
    private final TripFleetConverter fleetConverter;

    private Scenario(File dataDir, NamingConvention convention, //
            TripFleetConverter converter, //
            File processingDir, //
            LocalDate simulationDate, //
            AmodeusTimeConvert timeConvert) {
        GlobalAssert.that(dataDir.isDirectory());
        this.dataDir = dataDir;
        this.convention = convention;
        this.processingDir = processingDir;
        this.simulationDate = simulationDate;
        this.timeConvert = timeConvert;
        this.fleetConverter = converter;
    }

    private void run() throws Exception {
        InitialFiles.copyToDir(processingDir, dataDir);
        fleetConverter.setFilters();
        fleetConverter.run(processingDir, convention, simulationDate, timeConvert);
    }

    public Optional<File> getFinalTripFile() {
        return fleetConverter.getFinalTripFile();
    }
}
