/* amodeus - Copyright (c) 2018, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;

import ch.ethz.idsc.amodeus.matsim.xml.XmlCustomModifier;

public enum Pt2MatsimXML {
    ;

    public static void toLocalFileSystem(File xmlFile, String systemSpecificPath) throws Exception {

        System.out.println("xml file " + xmlFile.getAbsolutePath());

        try (XmlCustomModifier xmlModifier = new XmlCustomModifier(xmlFile)) {
            Document doc = xmlModifier.getDocument();
            Element rootNode = doc.getRootElement();
            Element module = rootNode.getChild("module");

            for (Element element : module.getChildren()) {
                String nameValue = element.getAttributeValue("name");
                String oldValue = element.getAttributeValue("value");
                if (nameValue == null)
                    continue;
                if (nameValue.equals("osmFile")) {
                    element.setAttribute("value", systemSpecificPath + "/" + oldValue);
                }
                if (nameValue.equals("outputNetworkFile")) {
                    element.setAttribute("value", systemSpecificPath + "/" + oldValue);
                }
            }
        }
    }

    public static void changeAttributes(File xmlFile, Map<String, String> map) throws Exception {

        System.out.println("xml file " + xmlFile.getAbsolutePath());

        try (XmlCustomModifier xmlModifier = new XmlCustomModifier(xmlFile)) {
            Document doc = xmlModifier.getDocument();
            Element rootNode = doc.getRootElement();
            Element module = rootNode.getChild("module");

            for (Element element : module.getChildren()) {
                String nameValue = element.getAttributeValue("name");
                if (nameValue == null)
                    continue;
                if (map.containsKey(nameValue)) {
                    element.setAttribute("value", map.get(nameValue));
                }
            }
        }
    }
}
