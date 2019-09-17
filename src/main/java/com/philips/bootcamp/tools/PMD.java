package com.philips.bootcamp.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.philips.bootcamp.domain.Tool;
import com.philips.bootcamp.utils.FileUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PMD implements Tool {

    public JsonObject parseXml(String out) {
        if (out == null) return null;
        JsonObject data = new JsonObject();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(out.getBytes()));

            // recommended
            document.getDocumentElement().normalize();
            NodeList files = document.getElementsByTagName("file");
            
            for (int index = 0; index < files.getLength(); index++) {

                JsonArray array = new JsonArray();

                Node file = files.item(index);
                NamedNodeMap attributes = file.getAttributes();
                String fileLocation = attributes.getNamedItem("name").getNodeValue();
                System.out.println(fileLocation);
                
                NodeList errors = ((Element) file).getElementsByTagName("violation");
                int errorLength = errors.getLength();
                
                for (int eindex = 0; eindex < errorLength; eindex++) {                
                    JsonObject object = new JsonObject();

                    Node error = errors.item(eindex);
                    NamedNodeMap errorDetails = error.getAttributes();
                    object.addProperty("line", errorDetails.getNamedItem("beginline").getNodeValue());
                    object.addProperty("priority", errorDetails.getNamedItem("priority").getNodeValue());
                    object.addProperty("message", error.getTextContent());

                    array.add(object);
                }

                data.add(fileLocation, array);
            }

        } catch (ParserConfigurationException pce) {
            return null;
        } catch (SAXException sax) {
            return null;
        } catch (IOException ioe) {
            return null;
        }

        return data;
    }

    @Override
    public JsonObject execute(JsonObject settings) {
        StringBuilder command = new StringBuilder("\"./../tools/pmd/bin/pmd.bat\"");
        command.append(" -d " + "\"" + settings.get("project").getAsString() + "\"");
        command.append(" -R " + settings.get("ruleset").getAsString());
        command.append(" -f xml");

        String out = run(command.toString());
        return parseXml(out);
    }

    @Override
    public String getName() {
        return "pmd";
    }

    @Override
    public String getDescription() {
        return FileUtils.getFileContents(new File(toolsDirectory, "pmd.desc"));
    }

    @Override
    public boolean verifySettings(JsonObject settings) {
        List<String> rulesets = List.of("rulesets/java/quickstart.xml");
        if (settings == null) return false;

        String value = null;
        if (settings.has("ruleset")) {
            value = settings.get("ruleset").getAsString();
            if (rulesets.contains(value)) ;
            else return false;
        }
        return true;
    }

    @Override
    public JsonObject getDefaultSettings() {
        JsonObject defaults = new JsonObject();
        defaults.addProperty("ruleset", "rulesets/java/quickstart.xml");
        return defaults;
    }
    
}