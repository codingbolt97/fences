package com.philips.bootcamp.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.philips.bootcamp.dal.ProjectDAO;
import com.philips.bootcamp.domain.Project;
import com.philips.bootcamp.domain.Tool;
import com.philips.bootcamp.tools.ToolName;
import com.philips.bootcamp.utils.FileUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    ProjectDAO projectDAO;

    File parent = new File("./../sources");

    public void setProjectDAO(ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public void setParentFile(File file) {
        parent = file;
    }

    @Override
    public List<Project> findAll() {
        return projectDAO.findAll();
    }

    @Override
    public Project find(String name) {
        return projectDAO.find(name);
    }

    @Override
    public boolean save(String project) {
        if (project == null) return false;

        JsonParser parser = new JsonParser();
        JsonObject projectJsonObject = new JsonObject();

        try {
            projectJsonObject = parser.parse(project).getAsJsonObject();
        } catch (JsonParseException|IllegalStateException exception) {
            return false;
        }

        if (projectJsonObject == null) return false;

        Project projectObject = new Project();
        if (!projectJsonObject.has("project_name")) return false;
        String projectName = projectJsonObject.get("project_name").getAsString();

        File projectFolder = new File(parent, projectName);
        
        if (!projectFolder.exists() || !projectFolder.isDirectory()) return false;

        Project existing = find(projectName);
        if (existing != null) return false;

        projectObject.setName(projectName);
        projectObject.setProjectCreationDate(new Date(System.currentTimeMillis()));
        projectDAO.save(projectObject);

        // refactor this
        if (projectJsonObject.has("settings")) {
            // all the tools user wants
            Set<String> tools = projectJsonObject.get("settings").getAsJsonObject().keySet();
            JsonObject effectiveSettings = new JsonObject();

            // loop through all the tools user wants
            for (String tool : tools) {
                JsonObject object = new JsonObject();
                ToolName toolname = getTool(tool);
                if (toolname == null) continue;

                JsonObject toolParams;
                try {
                    toolParams = parser.parse(toolname.getInstance().getDescription()).getAsJsonObject().get("parameters").getAsJsonObject();
                } catch (JsonParseException jpe) { continue; }

                Set<String> params = toolParams.keySet();
                for (String param : params) {
                    object.addProperty(param, toolParams.get(param).getAsJsonObject().get("default").getAsString());
                }

                JsonObject userSettings = projectJsonObject.get("settings").getAsJsonObject().get(tool).getAsJsonObject();
                Set<String> customSettings = userSettings.keySet();
                for (String customSetting : customSettings) {
                    object.addProperty(customSetting, userSettings.get(customSetting).getAsString());
                }
                effectiveSettings.add(tool, object);
            }
            
            File settingsJson = new File(projectFolder, "settings.json");
            FileUtils.writeFileContents(settingsJson, effectiveSettings.toString());
        }

        return true;
    }

    private ToolName getTool(String toolname) {
        switch(toolname.toLowerCase()) {
            case "checkstyle": return ToolName.CHECKSTYLE; 
            case "pmd": return ToolName.PMD; 
            case "maven": return ToolName.MAVEN;
        }

        return null;
    }

    @Override
    public boolean delete(String name) {
        Project existing = find(name);
        if (existing == null) return false;

        Path path = new File(parent, name).toPath();
        try { FileUtils.deleteDirectoryRecursion(path); } 
        catch (IOException ioe) { ; }

        projectDAO.delete(name);
        return true;
    }

    @Override
    public String buildProject(String name) {
        Project project = find(name);
        if (project == null) return "{\"error\" : \"No project found with the name: " + name + "\"}";

        JsonParser parser = new JsonParser();
        JsonObject projectSettings;

        File projectFolder = new File(parent, name);
        try {
            String fileContent = FileUtils.getFileContents(new File(projectFolder, "settings.json"));
            projectSettings = parser.parse(fileContent).getAsJsonObject();
        } catch (Exception jpe) {
            return "{\"error\" : \"Exception encountered while reading project settings\"}";
        }

        JsonObject report = new JsonObject();
        Set<String> tools = projectSettings.keySet();
        for (String tool : tools) {
            JsonObject toolSettings = projectSettings.get(tool).getAsJsonObject();
            toolSettings.addProperty("project", projectFolder.getAbsolutePath());
            JsonObject output = getTool(tool).getInstance().execute(toolSettings);
            report.add(tool, output);
        }

        FileUtils.writeFileContents(new File(projectFolder, "report.json"), report.toString());
        project.setLastBuildDate(new Date(System.currentTimeMillis()));
        projectDAO.update(project);

        return "{\"error\" : \"none\"}";
    }

    @Override
    public boolean updateSettings(String name, String settings) {
        if (name == null || settings == null) return false;

        Project project = find(name);
        if (project == null) return false;

        JsonParser parser = new JsonParser();
        JsonObject projectSettings = null, actualProjectSettings = null;

        File projectDirectoy = new File(parent, name);
        try {
            projectSettings = parser.parse(settings).getAsJsonObject();
            actualProjectSettings = parser.parse(FileUtils.getFileContents(new File(projectDirectoy, "settings.json"))).getAsJsonObject();
        } catch(Exception exception) {
            return false;
        }

        // projectSettings : { "checkstyle" : {}, "pmd" : {}}
        Set<String> tools = projectSettings.keySet();

        for (String tool : tools) {
            // check if tool exists
            ToolName iTool = getTool(tool);
            if (iTool == null) continue;

            Tool toolInstance = iTool.getInstance();
            JsonObject toolSettings = projectSettings.get(tool).getAsJsonObject();
            if (!toolInstance.verifySettings(toolSettings)) continue;

            JsonElement actualToolElement = actualProjectSettings.get(tool);
            if (actualToolElement == null) {
                // add the tool with default settings to actualProjectSettings
                actualProjectSettings.add(tool, toolInstance.getDefaultSettings());
            } 

            JsonObject actualToolSettings = actualProjectSettings.get(tool).getAsJsonObject();
            updateToolSettings(actualToolSettings, toolSettings);
        }

        Set<String> rTools = setDifference(actualProjectSettings.keySet(), tools);
        for (String tool : rTools) {
            actualProjectSettings.remove(tool);
        }

        FileUtils.writeFileContents(new File(projectDirectoy, "settings.json"), actualProjectSettings.toString());
        return true;
    }

    private Set<String> setDifference(Set<String> setA, Set<String> setB) {
        // setA - setB
        Set<String> difference = new HashSet<>();
        for (String string : setA) {
            if (!setB.contains(string)) 
                difference.add(string);
        } 
        return difference;
    }

    private void updateToolSettings(JsonObject actualToolSettings, JsonObject toolSettings) {
        Set<String> parameters = toolSettings.keySet();

        for (String param : parameters) {
            actualToolSettings.addProperty(param, toolSettings.get(param).getAsString());
        }
    }

    @Override
    public String getReport(String name) {
        return getFile(name, "report.json");
    }

    @Override
    public String getSettings(String name) {
        return getFile(name, "settings.json");
    }

    private String getFile(String projectName, String fileName) {
        if (projectName == null) return null;
        
        Project project = find(projectName);
        if (project == null) return null;
        
        File report = new File(parent, projectName + "/" + fileName);
        if (!report.exists()) return null;

        return FileUtils.getFileContents(report);
    }
}