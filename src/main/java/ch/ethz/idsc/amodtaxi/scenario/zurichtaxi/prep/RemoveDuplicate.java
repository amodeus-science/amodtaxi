/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario.zurichtaxi.prep;

/* package */ enum RemoveDuplicate {
    ;

    /** @return String identical to String @param original
     *         where every sequence of spaces longer than 1 is reduced to
     *         1 space */
    public static String spaces(String original) {
        String reduced = original.replace("  ", " ");
        if (reduced.length() == original.length())
            return reduced;
        return spaces(reduced);
    }

}
