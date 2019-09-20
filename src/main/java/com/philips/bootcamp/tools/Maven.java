package com.philips.bootcamp.tools;

import java.io.File;

import com.google.gson.JsonObject;
import com.philips.bootcamp.domain.Constants;
import com.philips.bootcamp.domain.Tool;
import com.philips.bootcamp.utils.FileUtils;
import com.philips.bootcamp.utils.TerminalUtils;

public class Maven implements Tool {

    @Override
    public JsonObject execute(JsonObject settings) {
        StringBuilder command = new StringBuilder("\"./../tools/maven.bat\"");

        command.append(" \"" + settings.get("project").getAsString() + "\"");
        command.append(" \"" + settings.get("command").getAsString() + "\"");

        String out = TerminalUtils.run(command.toString());
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
        return FileUtils.getFileContents(new File(Constants.toolsDirectory, "maven.desc"));
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

        return true;
    }

    @Override
    public JsonObject getDefaultSettings() {
        JsonObject defaults = new JsonObject();
        defaults.addProperty("command", "mvn package");
        return defaults;
    }

    @Override
    public JsonObject compare(JsonObject futureReport, JsonObject pastReport) {
        return null;
    }
}