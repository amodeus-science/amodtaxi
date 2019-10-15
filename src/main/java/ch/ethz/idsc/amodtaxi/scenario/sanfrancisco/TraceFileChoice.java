/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import ch.ethz.idsc.amodeus.util.math.GlobalAssert;

public class TraceFileChoice {

    private final List<File> taxiFiles;
    private Random randomizer = new Random(4);

    public TraceFileChoice(List<File> taxiFiles) {
        GlobalAssert.that(taxiFiles.size() > 0);
        this.taxiFiles = taxiFiles;
    }

    /** @return all trace files containing the @param names in their name , sample usage
     *         List<File> traceFiles = (new TraceFileChoice(taxiFiles)).specified("new_aupclik","new_ojumna") */
    public List<File> specified(String... nameSegments) {
        List<File> filesChosen = new ArrayList<>();
        for (String nameSegment : nameSegments) {
            taxiFiles.stream().forEach(f -> {
                if (f.getName().contains(nameSegment))
                    filesChosen.add(f);
            });
        }
        return filesChosen;
    }

    /** @return random choice of @param numTaxiTraces files */
    public List<File> random(int numTaxiTraces) {
        Set<File> filesChosen = new HashSet<>();
        while (filesChosen.size() < numTaxiTraces) {
            filesChosen.add(taxiFiles.get(randomizer.nextInt(taxiFiles.size())));
        }
        List<File> flsFinal = new ArrayList<>();
        filesChosen.stream().forEach(f -> flsFinal.add(f));
        return flsFinal;
    }
}
