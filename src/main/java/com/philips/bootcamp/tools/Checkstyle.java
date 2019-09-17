package com.philips.bootcamp.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

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

public class Checkstyle implements Tool {

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
                
                NodeList errors = ((Element) file).getElementsByTagName("error");
                int errorLength = errors.getLength();
                
                for (int eindex = 0; eindex < errorLength; eindex++) {                
                    JsonObject object = new JsonObject();

                    Node error = errors.item(eindex);
                    NamedNodeMap errorDetails = error.getAttributes();
                    object.addProperty("line", errorDetails.getNamedItem("line").getNodeValue());
                    object.addProperty("severity", errorDetails.getNamedItem("severity").getNodeValue());
                    object.addProperty("message", errorDetails.getNamedItem("message").getNodeValue());

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
        // construct command
        StringBuilder command = new StringBuilder("java -jar ./../tools/checkstyle-8.23-all.jar");
        
        // -c ?.xml
        command.append(" -c " + settings.get("styleguide").getAsString() + ".xml");

        // project location
        command.append(" \"" + settings.get("project").getAsString() + "\"");

        // output as xml
        command.append(" -f xml");

        // exclude target folder
        command.append(" -e target");

        // exclude test files?
        if (settings.get("excludeTestFiles").getAsString().equals("yes"))
            command.append(" -e src/test");

        // run command
        String out = run(command.toString());
        // handle output
        return parseXml(out);
    }

    @Override
    public String getName() {
        return "checkstyle";
    }

    @Override
    public String getDescription() {
        return FileUtils.getFileContents(new File(toolsDirectory, "checkstyle.desc"));
    }

    @Override
    public boolean verifySettings(JsonObject settings) {
        if (settings == null) return false;
    
        String value = null;

        if (settings.has("styleguide")) {
            value = settings.get("styleguide").getAsString();
            if (value.equals("sun_checks") || value.equals("google_checks")) ;
            else return false;
        }

        if (settings.has("excludeTestFiles")) {
            value = settings.get("excludeTestFiles").getAsString();
            if (value.equals("yes") || value.equals("no")) ;
            else return false;
        }

        return true;
    }

    @Override
    public JsonObject getDefaultSettings() {
        JsonObject defaults = new JsonObject();
        defaults.addProperty("styleguide", "google_checks");
        defaults.addProperty("excludeTestFiles", "no");
        return defaults;
    }

}