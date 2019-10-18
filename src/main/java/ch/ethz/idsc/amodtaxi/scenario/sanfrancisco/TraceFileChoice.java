/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.util.RandomElements;

public class TraceFileChoice {
    private final List<File> taxiFiles;

    public TraceFileChoice(List<File> taxiFiles) {
        GlobalAssert.that(taxiFiles.size() > 0);
        this.taxiFiles = taxiFiles;
    }

    /** @return all trace files containing the @param names in their name , sample usage
     *         List<File> traceFiles = (new TraceFileChoice(taxiFiles)).specified("new_aupclik","new_ojumna") */
    public List<File> specified(String... nameSegments) {
        List<File> filesChosen = new ArrayList<>();
        for (String nameSegment : nameSegments) {
            taxiFiles.stream().forEach(file -> {
                if (file.getName().contains(nameSegment))
                    filesChosen.add(file);
            });
        }
        return filesChosen;
    }

    /** @return random choice of @param numTaxiTraces files */
    public List<File> random(int numTaxiTraces) {
        return RandomElements.of(new ArrayList<>(taxiFiles), numTaxiTraces);
    }
}
