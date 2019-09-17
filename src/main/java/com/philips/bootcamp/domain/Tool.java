package com.philips.bootcamp.domain;

import java.io.File;

import com.google.gson.JsonObject;
import com.philips.bootcamp.utils.StreamUtils;

public interface Tool {
    File toolsDirectory = new File("./../tools");
    
    String getName();
    String getDescription();
    JsonObject execute(JsonObject settings);
    boolean verifySettings(JsonObject settings);
    JsonObject getDefaultSettings();

    default String run(String command) {
        String output = null;

        try {
            Process p = Runtime.getRuntime().exec(command);
            output = StreamUtils.getStreamContents(p.getInputStream());
        } catch (Exception exception) {
            return null;
        }

        return output;
    }
}