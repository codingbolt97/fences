package com.philips.bootcamp.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.gson.JsonObject;
import com.philips.bootcamp.dal.ProjectDAO;
import com.philips.bootcamp.domain.Project;
import com.philips.bootcamp.tools.Checkstyle;
import com.philips.bootcamp.utils.FileUtils;

import org.junit.Test;
import org.mockito.Mockito;

public class ProjectServiceImplTest {

    @Test
    public void findAll() {
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        ProjectServiceImpl psi = new ProjectServiceImpl();
        List<Project> projects = List.of(new Project());
        Mockito.when(dao.findAll()).thenReturn(projects);
        psi.setProjectDAO(dao);
        assertEquals(projects, psi.findAll());
    }

    @Test
    public void find() {
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        ProjectServiceImpl psi = new ProjectServiceImpl();
        Project project = new Project();
        Mockito.when(dao.find("name")).thenReturn(project);
        psi.setProjectDAO(dao);
        assertEquals(project, psi.find("name"));
    }

    @Test
    public void saveWhenWithProjectStringAsRandomText() {
        String randomtext = "isdbbskdc";
        ProjectServiceImpl psi = new ProjectServiceImpl();
        boolean result = psi.save(randomtext);
        assertFalse(result);
    }

    @Test
    public void saveWithProjectStringIsNull() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        boolean result = psi.save(null);
        assertFalse(result);
    }

    @Test
    public void saveWithProjectStringAsEmpty() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        boolean result = psi.save("");
        assertFalse(result);
    }

    @Test
    public void saveButProjectFolderDoesNotExist() {
        File parent = createDirectory("source");

        ProjectServiceImpl psi = new ProjectServiceImpl();
        psi.setParentFile(parent);

        assertFalse(psi.save("{\"project_name\":\"test\"}"));
        deleteDirectory(parent);
    }

    @Test
    public void saveButProjectFolderIsNotFolder() throws IOException {
        File parent = createDirectory("source");
        File test = new File(parent, "test");
        test.createNewFile();

        ProjectServiceImpl psi = new ProjectServiceImpl();
        psi.setParentFile(parent);

        assertFalse(psi.save("{\"project_name\":\"test\"}"));
        deleteDirectory(parent);
    }

    @Test
    public void saveButProjectAlreadyExists() throws IOException {
        File parent = createDirectory("source");
        File test = new File(parent, "test");
        test.mkdir();

        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("test")).thenReturn(new Project());
        psi.setProjectDAO(dao);
        psi.setParentFile(parent);

        assertFalse(psi.save("{\"project_name\":\"test\"}"));
        deleteDirectory(parent);
    }

    @Test
    public void saveButProjectHasNoSettings() throws IOException {
        File parent = createDirectory("source");
        File test = new File(parent, "test");
        test.mkdir();

        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("test")).thenReturn(null);
        Mockito.when(dao.save(Mockito.any(Project.class))).thenReturn(null);
        psi.setProjectDAO(dao);
        psi.setParentFile(parent);

        assertTrue(psi.save("{\"project_name\":\"test\"}"));
        deleteDirectory(parent);
    }

    @Test
    public void saveWithProjectSettings() throws IOException {
        File parent = createDirectory("source");
        File test = new File(parent, "test");
        test.mkdir();

        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("test")).thenReturn(null);
        Mockito.when(dao.save(Mockito.any(Project.class))).thenReturn(null);
        psi.setProjectDAO(dao);
        psi.setParentFile(parent);

        assertTrue(psi.save("{\"project_name\":\"test\", \"settings\":{}}"));
        deleteDirectory(parent);
    }

    @Test
    public void projectStringHasNoProjectName() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        boolean result = psi.save("{}");
        assertFalse(result);
    }

    @Test
    public void projectStringProperButDirectoryDoesntExist() {
        File parent = createDirectory("source");
        String project = "{\"project_name\" : \"test\"}";
        ProjectServiceImpl psi = new ProjectServiceImpl();
        psi.setParentFile(parent);
        boolean result = psi.save(project);
        assertFalse(result);
        deleteDirectory(parent);
    }

    @Test
    public void projectStringProperButNotADirectory() throws IOException {
        File parent = createDirectory("source");
        File test = new File(parent, "test");
        test.createNewFile();
        String project = "{\"project_name\" : \"test\"}";
        ProjectServiceImpl psi = new ProjectServiceImpl();
        psi.setParentFile(parent);
        boolean result = psi.save(project);
        assertFalse(result);
        deleteDirectory(parent);
    }

    @Test
    public void getReportShouldReturnNullWhenProjectNameIsNull() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        assertNull(psi.getReport(null));
    }

    @Test
    public void getReportShouldReturnNullWhenProjectDoesntexist() {
        ProjectServiceImpl psi = Mockito.mock(ProjectServiceImpl.class);
        Mockito.when(psi.find("project")).thenReturn(null);
        Mockito.when(psi.getReport("project")).thenCallRealMethod();
        assertNull(psi.getReport("project"));
    }

    @Test
    public void getReportShouldReturnReportWhenProjectReportExists() {
        File parent = createDirectory("source");
        File project = new File(parent, "project");
        project.mkdir();
        File report = new File(project, "report.json");
        FileUtils.writeFileContents(report, "contents");
        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("project")).thenReturn(new Project());
        psi.setParentFile(parent);
        psi.setProjectDAO(dao);
        String out = psi.getReport("project");
        assertEquals("contents\n", out);
        deleteDirectory(parent);
    }

    @Test
    public void getReportShouldReturnNullWhenProjectReportDoesntExists() {
        File parent = createDirectory("source");
        File project = new File(parent, "project");
        project.mkdir();
        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("project")).thenReturn(new Project());
        psi.setParentFile(parent);
        psi.setProjectDAO(dao);
        String out = psi.getReport("project");
        assertNull(out);
        deleteDirectory(parent);
    }

    @Test
    public void getSettingsShouldReturnNullWhenProjectNameIsNull() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        assertNull(psi.getSettings(null));
    }

    @Test
    public void returnFalseWhenDeletingAProjectThatDoesnNotExist() {
        ProjectServiceImpl psi = Mockito.mock(ProjectServiceImpl.class);
        Mockito.when(psi.find("null")).thenReturn(null);
        Mockito.when(psi.delete("null")).thenCallRealMethod();
        assertFalse(psi.delete("null"));
    }

    @Test
    public void returnTrueWhenDeletingAProject() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("project")).thenReturn(new Project());
        File parent = createDirectory("source");
        File project = new File(parent, "project");
        project.mkdir();
        psi.setProjectDAO(dao);
        psi.setParentFile(parent);
        assertTrue(psi.delete("project"));
        assertFalse(project.exists());
        deleteDirectory(parent);
    }

    @Test
    public void buildFailsWhenProjectDoesNotExist() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("project")).thenReturn(null);
        psi.setProjectDAO(dao);
        assertEquals("{\"error\" : \"No project found with the name: project\"}", psi.buildProject("project"));
    }

    @Test
    public void buildFailsWhenSettingsFileNotAvailable() {
        File parent = createDirectory("source");
        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("project")).thenReturn(new Project());
        psi.setProjectDAO(dao);
        assertEquals("{\"error\" : \"Exception encountered while reading project settings\"}", psi.buildProject("project"));
        deleteDirectory(parent);
    }

    @Test
    public void buildSucceedsWithProperArgs() throws IOException {
        File parent = createDirectory("source");
        File project = new File(parent, "project");
        project.mkdir();
        JsonObject settings = new JsonObject();
        settings.add("checkstyle", new Checkstyle().getDefaultSettings());
        File settingsFile = new File(project, "settings.json");
        FileUtils.writeFileContents(settingsFile, settings.toString()); 

        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("project")).thenReturn(new Project());
        psi.setProjectDAO(dao);
        psi.setParentFile(parent);
        assertEquals("{\"error\" : \"none\"}", psi.buildProject("project"));
        deleteDirectory(parent);
    }

    @Test
    public void updateSettingsReturnsFalseWhenNameIsNull() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        assertFalse(psi.updateSettings(null, "settings"));
    }

    @Test
    public void updateSettingsReturnsFalseWhenSettingsIsNull() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        assertFalse(psi.updateSettings("name", null));
    }

    @Test
    public void updateSettingsReturnsFalseWhenProjectDoesNotExist() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("name")).thenReturn(null);
        psi.setProjectDAO(dao);
        assertFalse(psi.updateSettings("name", "settings"));
    }

    @Test
    public void updateSettingsReturnsFalseWhenSettingsIsInvalid() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("name")).thenReturn(new Project());
        psi.setProjectDAO(dao);
        assertFalse(psi.updateSettings("name", "settings"));
    }

    @Test
    public void updateSettingsTest() {
        File parent = createDirectory("source");
        File project = new File(parent, "project");
        project.mkdir();
        File settingJson = new File(project, "settings.json");
        FileUtils.writeFileContents(settingJson, "{}");

        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("project")).thenReturn(new Project());
        psi.setProjectDAO(dao);
        psi.setParentFile(parent);

        assertTrue(psi.updateSettings("project", "{\"checkstyle\":{}}"));
        deleteDirectory(parent);
    }

    @Test
    public void updateSettingsTestv2() {
        File parent = createDirectory("source");
        File project = new File(parent, "project");
        project.mkdir();
        File settingJson = new File(project, "settings.json");
        FileUtils.writeFileContents(settingJson, "{\"mkd\":{}}");

        ProjectServiceImpl psi = new ProjectServiceImpl();
        ProjectDAO dao = Mockito.mock(ProjectDAO.class);
        Mockito.when(dao.find("project")).thenReturn(new Project());
        psi.setProjectDAO(dao);
        psi.setParentFile(parent);

        assertTrue(psi.updateSettings("project", "{\"checkstyle\":{}, \"pop\":{}}"));
        deleteDirectory(parent);
    }

    // how do you mock this?
    public void getToolsTest() {
        ProjectServiceImpl psi = new ProjectServiceImpl();
        String tools = psi.getTools();
        System.out.println(tools);
    }

    private File createDirectory(String parent) {
        File file = new File(parent);
        file.mkdir();

        return file;
    }

    private void deleteDirectory(File directory) {
        try {
            FileUtils.deleteDirectoryRecursion(directory.toPath());
        } catch (IOException e) {
            System.out.println("Error deleting directory");
        }
    }
}