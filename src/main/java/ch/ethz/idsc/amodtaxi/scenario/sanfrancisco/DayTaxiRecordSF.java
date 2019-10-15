/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;
import java.util.TreeSet;

import ch.ethz.idsc.amodeus.net.FastLinkLookup;
import ch.ethz.idsc.amodeus.net.IdIntegerDatabase;
import ch.ethz.idsc.amodeus.net.MatsimAmodeusDatabase;
import ch.ethz.idsc.amodeus.util.math.GlobalAssert;
import ch.ethz.idsc.amodtaxi.trace.DayTaxiRecord;

public class DayTaxiRecordSF implements DayTaxiRecord {

    protected final List<TaxiTrail> trails = new ArrayList<>();
    private final IdIntegerDatabase vehicleIdIntegerDatabase = new IdIntegerDatabase();
    private final MatsimAmodeusDatabase db;
    private final FastLinkLookup qt;

    public DayTaxiRecordSF(MatsimAmodeusDatabase db, FastLinkLookup qt) {
        this.db = db;
        this.qt = qt;
    }

    @Override
    public void insert(List<String> list, int taxiStampID, String id) {

        final int taxiStamp_id = vehicleIdIntegerDatabase.getId(Integer.toString(taxiStampID));
        if (taxiStamp_id == trails.size()) {
            trails.add(new TaxiTrailSF(id, db, qt));
            System.out.println("Trails: " + trails.size());
        }
        trails.get(taxiStamp_id).insert(list);
    }

    @Override
    public void processFilledTrails() throws Exception {
        GlobalAssert.that(trails.size() > 0);
        int requestIndex = 1;
        for (TaxiTrail taxiTrail : trails) {
            taxiTrail.processFilledTrail();

            // GlobalAssert.that(taxiTrail instanceof TaxiTrailSF);
            // TaxiTrailSF taxiTrailSF = (TaxiTrailSF) taxiTrail;
            // int requestIndexUsed = taxiTrailSF.setRequestContainers(requestIndex, db, qt);
            // requestIndex = requestIndexUsed + 1;
            // GlobalAssert.that(requestIndex >= 0);
        }
    }

    // /** this should only be run after initialization, for every time step, it
    // * creates a RequestContainer */
    // public void processRoboTaxiStatus() {
    // GlobalAssert.that(trails.size() > 0);
    // int requestIndex = 1;
    // for (TaxiTrailInterface taxiTrail : trails) {
    // GlobalAssert.that(taxiTrail instanceof TaxiTrailSF);
    // TaxiTrailSF taxiTrailSF = (TaxiTrailSF) taxiTrail;
    // taxiTrailSF.setRoboTaxiStatus();
    // }
    //
    // }

    @Override
    public LocalDateTime getMaxTime() {
        NavigableSet<LocalDateTime> times = new TreeSet<>();
        for (TaxiTrail tt : trails) {
            times.add(tt.getMaxTime());
        }
        return times.last();

    }

    @Override
    public int numTaxis() {
        return trails.size();
    }

    @Override
    public List<TaxiTrail> getTrails() {
        return trails;

    }

    @Override
    public TaxiTrail get(int vehicleIndex) {
        return trails.get(vehicleIndex);
    }

}
