//package ch.ethz.idsc.amodtaxi.scenario.sanfrancisco;
//
//import org.matsim.api.core.v01.network.Network;
//
//public enum NetworkSpeedUtil {
//    ;
//
//    private static double globalMinimum = 40;
//    private static double motorwaySpeed = 31.2928;
//    private static double trunkSpeed = 24.5872; // 55 mph
//    private static double unclassifiedSpeed = 24.5872; // 55 mph
//
//    private static double globalFactor = 4.0;
//
//    public static void adapt(Network network) {
//
//        network.getLinks().values().forEach(l -> {
//
//            // NOW CRAZY METHOD
//            // l.setFreespeed(l.getFreespeed()*globalFactor);
//
//            // BEFORE
////            String attrb = (String) l.getAttributes().getAsMap().get("osm:way:highway");
////
////            /** setting motorways to motorwaySpeed */
////            if (attrb.contains("motorway")) {
////                l.setFreespeed(motorwaySpeed);
////            }
////
////            /** trunk roads to trunk speed */
////            if (attrb.contains("trunk")) {
////                l.setFreespeed(trunkSpeed);
////            }
////
////            /** unclassified roads to unclassified speed */
////            if (attrb.contains("unclassified"))
////                l.setFreespeed(unclassifiedSpeed);
//
//            /** global minimum of 20 km/h */
//            if (l.getFreespeed() < globalMinimum)
//                l.setFreespeed(globalMinimum);
//
//        });
//
//        /** minimum speed global */
//    }
//}
