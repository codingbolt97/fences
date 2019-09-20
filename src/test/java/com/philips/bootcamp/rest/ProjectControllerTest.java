package com.philips.bootcamp.rest;

import static org.junit.Assert.assertEquals;

import java.util.List;

import com.philips.bootcamp.domain.Project;
import com.philips.bootcamp.service.ProjectService;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ProjectControllerTest {
	/*
    @Test
    public void getProjectsList() {
        List<Project> projects = List.of(new Project());
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.findAll()).thenReturn(projects);
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        assertEquals(projects, controller.getProjectsList());
    }

    @Test
    public void getProjectNotExisting() {
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.find("project")).thenReturn(null);
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        assertEquals(HttpStatus.NOT_FOUND, controller.getProject("project").getStatusCode());
    }

    
    @Test
    public void getProjectExisting() {
        Project project = new Project();
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.find("project")).thenReturn(project);
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        ResponseEntity<Project> response = controller.getProject("project");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(project, response.getBody());
    }

    @Test
    public void createNewProjectError() {
        String project = "project";
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.save(project)).thenReturn(false);
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        assertEquals(HttpStatus.BAD_REQUEST, controller.createNewProject(project).getStatusCode());
    }

    @Test
    public void createnewProjectSuccess() {
        String project = "project";
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.save(project)).thenReturn(true);
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        assertEquals(HttpStatus.CREATED, controller.createNewProject(project).getStatusCode());
    }

    @Test
    public void deleteNonxistingProject() {
        String project = "project";
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.delete(project)).thenReturn(false);
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        assertEquals(HttpStatus.NOT_FOUND, controller.deleteProject(project).getStatusCode());
    }

    @Test
    public void deleteExistigProject() {
        String project = "project";
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.delete(project)).thenReturn(true);
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        assertEquals(HttpStatus.NO_CONTENT, controller.deleteProject(project).getStatusCode());
    }

    @Test
    public void getToolsList() {
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.getTools()).thenReturn("tools");
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        assertEquals("tools", controller.getToolsList());
    }

    @Test
    public void updateProjectSettingsSuccessful() {
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.updateSettings("project", "settings")).thenReturn(true);
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        assertEquals(HttpStatus.NO_CONTENT, controller.updateProjectSettings("project", "settings").getStatusCode());
    }

    @Test
    public void errorInUpdatingProjectSettings() {
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.updateSettings("project", "settings")).thenReturn(false);
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        assertEquals(HttpStatus.BAD_REQUEST, controller.updateProjectSettings("project", "settings").getStatusCode());
    }

    @Test
    public void getProjectSettingsWhenProjectExists() {
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.getSettings("project")).thenReturn("settings");
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        ResponseEntity<String> response = controller.getProjectSettings("project");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("settings", response.getBody());
    }

    @Test
    public void getProjectSettingsWhenProjectDoesNotExist() {
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.getSettings("project")).thenReturn(null);
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        ResponseEntity<String> response = controller.getProjectSettings("project");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void getProjectReportWhenProjectExists() {
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.getReport("project")).thenReturn("report");
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        ResponseEntity<String> response = controller.getProjectReport("project");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("report", response.getBody());
    }

    @Test
    public void getProjectReportWhenProjectDoesNotExist() {
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.getReport("project")).thenReturn(null);
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        ResponseEntity<String> response = controller.getProjectReport("project");
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    public void buildProject() {
        ProjectService ps = Mockito.mock(ProjectService.class);
        Mockito.when(ps.buildProject("project")).thenReturn("value");
        ProjectController controller = new ProjectController();
        controller.setService(ps);

        assertEquals("value", controller.buildProject("project"));
    }
    */
}