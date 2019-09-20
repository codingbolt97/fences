package com.philips.bootcamp.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.philips.bootcamp.domain.Constants;
import com.philips.bootcamp.domain.Tool;
import com.philips.bootcamp.utils.FileUtils;
import com.philips.bootcamp.utils.TerminalUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Checkstyle implements Tool {

    public JsonObject parseXml(String out) {
        if (out == null) return null;

        int noOfErrors = 0;
        JsonObject report = new JsonObject();
        JsonObject metrics = new JsonObject();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(out.getBytes()));

            // recommended
            document.getDocumentElement().normalize();
            NodeList files = document.getElementsByTagName("file");

            noOfErrors = document.getElementsByTagName("error").getLength();
            metrics.addProperty("errors", noOfErrors);
            
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

                report.add(fileLocation, array);
            }

        } catch (ParserConfigurationException pce) {
            return null;
        } catch (SAXException sax) {
            return null;
        } catch (IOException ioe) {
            return null;
        }
        
        JsonObject data = new JsonObject();
        data.add("report", report);
        data.add("metrics", metrics);
        return data;
    }

    @Override
    public JsonObject execute(JsonObject settings) {
        // construct command
        StringBuilder command = new StringBuilder("java -jar");
        
        // path to checkstyle jar
        command.append(" \"" + new File(Constants.toolsDirectory, "checkstyle-8.23-all.jar").getAbsolutePath() + "\"");

        // -c ?.xml
        command.append(" -c " + settings.get("styleguide").getAsString() + ".xml");

        // project location
        command.append(" \"" + settings.get("project").getAsString() + "\"");

        // output as xml
        command.append(" -f xml");

        // redirect to output file
        command.append(" -o \"" + Constants.output.getAbsolutePath() + "\"");

        // exclude target folder
        command.append(" -e target");

        // exclude test files?
        if (settings.get("excludeTestFiles").getAsString().equals("yes"))
            command.append(" -e src/test");

        // run command
        TerminalUtils.run(command.toString());

        return parseXml(FileUtils.getFileContents(Constants.output));
    }

    @Override
    public String getName() {
        return "checkstyle";
    }

    @Override
    public String getDescription() {
        return FileUtils.getFileContents(new File(Constants.toolsDirectory, "checkstyle.desc"));
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

    @Override
    public JsonObject compare(JsonObject futureReport, JsonObject pastReport) {
        JsonObject comparison = new JsonObject();
        double percentage = 0.0;

        DecimalFormat df = new DecimalFormat("#.###");
        
        if (pastReport != null && futureReport != null) {
            int errorsThen = pastReport.get("metrics").getAsJsonObject().get("errors").getAsInt();
            int errorsNow = futureReport.get("metrics").getAsJsonObject().get("errors").getAsInt();

            if (errorsThen != -1) 
                percentage = (errorsThen - errorsNow) * 1f / (errorsThen * 1f);

            comparison.addProperty("errorsThen", errorsThen);
            comparison.addProperty("errorsNow", errorsNow);
            comparison.addProperty("percentageChange", df.format(percentage));

            return comparison;

        } else if (pastReport == null && futureReport != null) {
            int errorsNow = futureReport.get("metrics").getAsJsonObject().get("errors").getAsInt();
            
            comparison.addProperty("errorsThen", "null");
            comparison.addProperty("errorsNow", errorsNow);
            comparison.addProperty("percentageChange", "null");

            return comparison;
        } else {
            comparison.addProperty("errorsThen", "null");
            comparison.addProperty("errorsNow", "null");
            comparison.addProperty("percentageChange", "null");

            return comparison;
        }
    }
}