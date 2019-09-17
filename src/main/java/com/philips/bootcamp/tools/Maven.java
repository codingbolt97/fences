package com.philips.bootcamp.tools;

import java.io.File;

import com.google.gson.JsonObject;
import com.philips.bootcamp.domain.Tool;
import com.philips.bootcamp.utils.FileUtils;

public class Maven implements Tool {

    @Override
    public JsonObject execute(JsonObject settings) {
        StringBuilder command = new StringBuilder("\"./../tools/maven.bat\"");

        command.append(" \"" + settings.get("project").getAsString() + "\"");
        command.append(" \"" + settings.get("command").getAsString() + "\"");

        System.out.println(command.toString());
        String out = run(command.toString());
        System.out.println(out);
        return handleOutput(out);
    }

    public JsonObject handleOutput(String out) {
        if (out == null) return null;

        JsonObject report = new JsonObject();
        // do something to figure out of build is successful or not
        if (out.contains("BUILD SUCCESS")) report.addProperty("buildStatus", "success");
        else report.addProperty("buildStatus", "failure");

        report.addProperty("details", out);
        return report;
    }

    @Override
    public String getName() {
        return "maven";
    }

    @Override
    public String getDescription() {
        return FileUtils.getFileContents(new File(toolsDirectory, "maven.desc"));
    }

    @Override
    public boolean verifySettings(JsonObject settings) {
        if (settings == null) return false;
        
        String value = null;

        if (settings.has("command")) {
            value = settings.get("command").getAsString();
            if (value.startsWith("mvn")) ;
            else return false;
        }

        if (settings.has("generateTestReport")) {
            value = settings.get("generateTestReport").getAsString();
            if (value.equals("yes") || value.equals("no")) ;
            else return false;
        }

        return true;
    }

    @Override
    public JsonObject getDefaultSettings() {
        JsonObject defaults = new JsonObject();
        defaults.addProperty("command", "mvn package");
        defaults.addProperty("generateTestReport", "no");
        return defaults;
    }
}