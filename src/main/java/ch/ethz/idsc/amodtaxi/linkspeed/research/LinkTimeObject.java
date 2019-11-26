/* amodeus - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.linkspeed.research;

/* package */ class LinkTimeObject {

    public int link;
    public int time;

    public static void main(String[] args) {

        LinkTimeObject obj1 = new LinkTimeObject();
        obj1.link = 1;
        obj1.time = 3;

        LinkTimeObject obj2 = new LinkTimeObject();
        obj2.link = 1;
        obj2.time = 3;

        // TODO check out override equal...
        System.out.println(obj1.equals(obj2));

    }

}
