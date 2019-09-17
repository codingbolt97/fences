package com.philips.bootcamp.service;

import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.philips.bootcamp.domain.Project;
import com.philips.bootcamp.tools.ToolName;

public interface ProjectService {
    String buildProject(String name);
    List<Project> findAll();
    Project find(String name);
    boolean save(String project);
    boolean delete(String name);

    boolean updateSettings(String name, String settings);
    String getReport(String name);
    String getSettings(String name);

    default String getTools() {
        JsonParser parser = new JsonParser();
        JsonObject tools = new JsonObject();
        for (ToolName tool : ToolName.values()) {
            JsonObject toolObject = null;
            try {
                toolObject = parser.parse(tool.getInstance().getDescription()).getAsJsonObject();
            } catch (JsonParseException jpe) {}
            tools.add(tool.toString(), toolObject);
        }
        return tools.toString();
    }
}