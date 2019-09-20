package com.philips.bootcamp.service;

import java.io.File;
import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.philips.bootcamp.dal.ProjectDAO;
import com.philips.bootcamp.domain.Constants;
import com.philips.bootcamp.domain.Project;
import com.philips.bootcamp.domain.Tool;
import com.philips.bootcamp.tools.ToolName;
import com.philips.bootcamp.utils.FileUtils;
import com.philips.bootcamp.utils.StringUtils;
import com.philips.bootcamp.utils.TerminalUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    ProjectDAO projectDAO;

    File parent = Constants.sourceDirectory;

    public void setProjectDAO(ProjectDAO projectDAO) {
        this.projectDAO = projectDAO;
    }

    public void setParentFile(File parent) {
        this.parent = parent;
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
    public String save(String project) {
        if (project == null) return "Invalid project json";

        JsonParser parser = new JsonParser();
        JsonObject projectJsonObject = new JsonObject();

        try {
            projectJsonObject = parser.parse(project).getAsJsonObject();
        } catch (JsonParseException|IllegalStateException exception) {
            return "Invalid project json";
        }

        if (projectJsonObject == null) return "Invalid project json";

        if (!projectJsonObject.has("link")) return "Project json must have property 'link'";
        if (!projectJsonObject.has("branch")) return "Project json must have property 'branch'";

        String projectLink = projectJsonObject.get("link").getAsString();
        String projectName = StringUtils.getProjectNameFromHttpLink(projectLink);
        if (projectName == null) return "Illegal property value for 'link'"; 

        String projectBranch = projectJsonObject.get("branch").getAsString();
        if (projectBranch == null) return "Illegal property value for 'branch'";

        Project projectObject = new Project();
        Project existing = find(projectName);
        if (existing == null) {
            projectObject.setName(projectName + "-" + projectBranch);
            projectObject.setProjectCreationDate(new Date(System.currentTimeMillis()));
            projectDAO.save(projectObject);
        }

        File projectFolder = new File(parent, projectName + "-" + projectBranch);
        
        if (projectFolder.exists()) {
            FileUtils.deleteFolder(projectFolder);
        }

        String cloneResult = cloneGitProject(projectLink);
        if (cloneResult != null) return cloneResult;

        FileUtils.renameFile(projectFolder, projectName + "-" + projectBranch);
        switchBranch(projectName + "-" + projectBranch, projectBranch);

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
            
            File settingsFolder = new File(Constants.dataDirectory, projectName + "-" + projectBranch);
            settingsFolder.mkdir();

            File settingsJson = new File(settingsFolder, "settings.json");
            FileUtils.writeFileContents(settingsJson, effectiveSettings.toString());
        }

        return (existing == null)? "Project created" : "Project updated";
    }

    private String cloneGitProject(String link) {
        StringBuilder command = new StringBuilder();
        command.append("\"" + new File(Constants.toolsDirectory, "action.bat").getAbsolutePath() + "\"");
        command.append(" \"" + Constants.sourceDirectory.getAbsolutePath() + "\"");
        command.append(" \"git clone " + link + "\"");
        String output = TerminalUtils.run(command.toString());
        if (output.contains("Resolving deltas: 100%")) {
            return null;
        }

        return output;
    }

    private void switchBranch(String project, String branch) {
        StringBuilder command = new StringBuilder();
        command.append("\"" + new File(Constants.toolsDirectory, "switch.bat").getAbsolutePath() + "\"");
        command.append(" \"" + new File(Constants.sourceDirectory, project).getAbsolutePath() + "\"");
        command.append(" " + branch);
        TerminalUtils.run(command.toString());
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

        File folder = new File(Constants.sourceDirectory, name);
        FileUtils.deleteFolder(folder);

        folder = new File(Constants.dataDirectory, name);
        FileUtils.deleteFolder(folder);

        projectDAO.delete(name);
        return true;
    }

    @Override
    public String fenceProject(String name) {
        Project project = find(name);
        if (project == null) return "{\"status\":\"fail\",\"error\" : \"No project found with the name: " + name + "\"}";

        JsonParser parser = new JsonParser();
        JsonObject projectSettings;

        File projectFolder = new File(Constants.sourceDirectory, name);
        try {
            String fileContent = FileUtils.getFileContents(new File(projectFolder, "settings.json"));
            projectSettings = parser.parse(fileContent).getAsJsonObject();
        } catch (Exception jpe) {
            return "{\"status\":\"fail\",\"error\" : \"Exception encountered while reading project settings\"}";
        }

        File projectDataDirectory = new File(Constants.dataDirectory, name);
        JsonObject prevReport;
        try {
            String fileContent = FileUtils.getFileContents(new File(projectDataDirectory, "report.json"));
            prevReport = parser.parse(fileContent).getAsJsonObject(); 
        } catch (Exception e) {
            prevReport = new JsonObject();
        }

        JsonObject comparisons = new JsonObject();
        JsonObject report = new JsonObject();

        Set<String> tools = projectSettings.keySet();
        for (String tool : tools) {
            JsonObject toolSettings = projectSettings.get(tool).getAsJsonObject();
            toolSettings.addProperty("project", projectFolder.getAbsolutePath());
            JsonObject output = getTool(tool).getInstance().execute(toolSettings);
            report.add(tool, output);

            JsonElement prevToolReport = prevReport.get(tool);
            if (prevToolReport != null) {
                JsonObject comparison = getTool(tool).getInstance().compare(output, prevToolReport.getAsJsonObject());
                comparison.add(tool, comparison);
            }
        }

        FileUtils.writeFileContents(new File(projectDataDirectory, "report.json"), report.toString());
        project.setLastBuildDate(new Date(System.currentTimeMillis()));
        projectDAO.update(project);

        return "{\"status\":\"pass\",\"report\" : " + comparisons.toString() + "}";
    }

    @Override
    public String updateSettings(String name, String settings) {
        if (name == null) return "Project name required";
        if (settings == null) return "No settings provided";

        Project project = find(name);
        if (project == null) return "No project found with name: " + name;

        JsonParser parser = new JsonParser();
        JsonObject projectSettings = null, actualProjectSettings = null;

        File projectDataDirectory = new File(Constants.dataDirectory, name);
        try {
            projectSettings = parser.parse(settings).getAsJsonObject();
        } catch(Exception exception) {
            return "Invalid settings json string";
        }

        try {
            actualProjectSettings = parser.parse(FileUtils.getFileContents(new File(projectDataDirectory, "settings.json"))).getAsJsonObject();
        } catch(Exception exception) {
            actualProjectSettings = new JsonObject();
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

        FileUtils.writeFileContents(new File(projectDataDirectory, "settings.json"), actualProjectSettings.toString());
        return "Project settings updated";
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
        if (projectName == null) return "Project name required";
        
        Project project = find(projectName);
        if (project == null) return "No project by name: " + projectName;
        
        File file = new File(Constants.dataDirectory, projectName + "/" + fileName);
        if (!file.exists()) return "File not present";

        return FileUtils.getFileContents(file);
    }

    @Override
    public String getInstantReport(String toolname, String source) {
        if (toolname == null) return "{\"error\" : \"Invalid toolname\"}";
        if (source == null) return "{\"error\" : \"No content provided\"}";

        ToolName toolName = getTool(toolname);
        if (toolName == null) return "{\"error\" : \"No such tool\"}";

        File testFile = new File(Constants.sampleDirectory, "Test.java");
        FileUtils.writeFileContents(testFile, source);
        Tool tool = toolName.getInstance();
        JsonObject defaultSettings = tool.getDefaultSettings();
        defaultSettings.addProperty("project", Constants.sampleDirectory.getAbsolutePath());
        JsonObject report = tool.execute(defaultSettings);
        report.addProperty("error", "none");
        return report.toString();
    }
}