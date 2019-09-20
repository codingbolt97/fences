package com.philips.bootcamp.tools;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

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

public class PMD implements Tool {

    public JsonObject parseXml(String out) {
        if (out == null) return null;
        
        JsonObject report = new JsonObject();
        JsonObject metrics = new JsonObject();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dbFactory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(out.getBytes()));

            // recommended
            document.getDocumentElement().normalize();
            NodeList files = document.getElementsByTagName("file");

            int noOfViolations = document.getElementsByTagName("violation").getLength();
            metrics.addProperty("errors", noOfViolations);
            
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
        StringBuilder command = new StringBuilder();
        command.append("\"" + new File(Constants.toolsDirectory, "pmd/bin/pmd.bat").getAbsolutePath() + "\"");
        command.append(" -d " + "\"" + settings.get("project").getAsString() + "\"");
        command.append(" -R " + settings.get("ruleset").getAsString());
        command.append(" -f xml");
        command.append(" -r " + "\"" + Constants.output.getAbsolutePath() + "\"");

        TerminalUtils.run(command.toString());
        return parseXml(FileUtils.getFileContents(Constants.output));
    }

    @Override
    public String getName() {
        return "pmd";
    }

    @Override
    public String getDescription() {
        return FileUtils.getFileContents(new File(Constants.toolsDirectory, "pmd.desc"));
    }

    @Override
    public boolean verifySettings(JsonObject settings) {
        List<String> rulesets = List.of("rulesets/java/quickstart.xml");
        if (settings == null) return false;

        String value = null;
        if (settings.has("ruleset")) {
            value = settings.get("ruleset").getAsString();
            if (!rulesets.contains(value)) ;
                return false;
        }

        return true;
    }

    @Override
    public JsonObject getDefaultSettings() {
        JsonObject defaults = new JsonObject();
        defaults.addProperty("ruleset", "rulesets/java/quickstart.xml");
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