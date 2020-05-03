/* amodtaxi - Copyright (c) 2019, ETH Zurich, Institute for Dynamic Systems and Control */
package ch.ethz.idsc.amodtaxi.scenario;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.io.Files;

import ch.ethz.idsc.amodeus.matsim.xml.XmlCustomModifier;
import ch.ethz.idsc.amodeus.util.io.Locate;

public class Pt2MatsimXMLTest {

    @Test
    public void testLocalFileSystem() throws Exception {
        /* Init */
        File workingDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "test");
        File resourcesDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/chicagoScenario");
        File ptFile = new File(workingDir, ScenarioLabels.pt2MatSettings);
        Assert.assertTrue(workingDir.exists() || workingDir.mkdir());
        Files.copy(new File(resourcesDir, ScenarioLabels.pt2MatSettings), ptFile);
        Assert.assertTrue(ptFile.exists());

        /* Run function of interest */
        Pt2MatsimXML.toLocalFileSystem(ptFile, workingDir.getAbsolutePath());

        /* Check functionality */
        try (XmlCustomModifier xmlModifier = new XmlCustomModifier(ptFile)) {
            Document doc = xmlModifier.getDocument();
            Element rootNode = doc.getRootElement();
            Element module = rootNode.getChild("module");

            for (Element element : module.getChildren()) {
                String nameValue = element.getAttributeValue("name");
                if (nameValue == null)
                    continue;
                if (nameValue.equals("osmFile")) {
                    Assert.assertTrue(element.getAttributeValue("value").contains(workingDir.getAbsolutePath()));
                }
                if (nameValue.equals("outputNetworkFile")) {
                    Assert.assertTrue(element.getAttributeValue("value").contains(workingDir.getAbsolutePath()));
                }
            }
        }

        /* Clean up */
        Assert.assertTrue(workingDir.exists());
        FileUtils.deleteDirectory(workingDir);
        Assert.assertFalse(workingDir.exists());
    }

    @Test
    public void testChangeAttribute() throws Exception {
        /* Init */
        File workingDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "test");
        File resourcesDir = new File(Locate.repoFolder(Pt2MatsimXML.class, "amodtaxi"), "resources/chicagoScenario");
        File ptFile = new File(workingDir, ScenarioLabels.pt2MatSettings);
        Assert.assertTrue(workingDir.exists() || workingDir.mkdir());
        Files.copy(new File(resourcesDir, ScenarioLabels.pt2MatSettings), ptFile);
        Assert.assertTrue(ptFile.exists());

        /* Run function of interest */
        Assert.assertTrue(ptFile.exists());
        Map<String, String> map = new HashMap<String, String>();
        map.put("outputCoordinateSystem", "EPSG:21781");
        Pt2MatsimXML.changeAttributes(ptFile, map);

        /* Check functionality */
        try (XmlCustomModifier xmlModifier = new XmlCustomModifier(ptFile)) {
            Document doc = xmlModifier.getDocument();
            Element rootNode = doc.getRootElement();
            Element module = rootNode.getChild("module");

            for (Element element : module.getChildren()) {
                String nameValue = element.getAttributeValue("name");
                if (nameValue == null)
                    continue;
                if (nameValue.equals("outputCoordinateSystem")) {
                    Assert.assertTrue(element.getAttributeValue("value").equals("EPSG:21781"));
                }
            }
        }

        /* Clean up */
        Assert.assertTrue(workingDir.exists());
        FileUtils.deleteDirectory(workingDir);
        Assert.assertFalse(workingDir.exists());
    }
}
