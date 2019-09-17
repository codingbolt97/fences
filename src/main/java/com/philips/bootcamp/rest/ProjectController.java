package com.philips.bootcamp.rest;

import java.util.List;

import com.philips.bootcamp.domain.Project;
import com.philips.bootcamp.service.ProjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProjectController {

    @Autowired
    ProjectService service;

    public void setService(ProjectService service) {
        this.service = service;
    }
    
    @RequestMapping(value = "/api/project", method = RequestMethod.GET)
    public List<Project> getProjectsList() {
        return service.findAll();
    }

    @RequestMapping(value = "/api/project/{name}", method = RequestMethod.GET)
    public ResponseEntity<Project> getProject(@PathVariable("name") String name) {
        Project project = service.find(name);
        if (project == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity<>(project, HttpStatus.OK);
        }
    }

    @RequestMapping(value = "/api/project", method = RequestMethod.POST)
    public ResponseEntity<Object> createNewProject(@RequestBody String project) {
        
        boolean result = service.save(project);
        if (result) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }        
    }

    @RequestMapping(value = "/api/project/{name}", method = RequestMethod.DELETE)
    public ResponseEntity<Object> deleteProject(@PathVariable("name") String name) {

        boolean result = service.delete(name);
        ResponseEntity<Object> response = null;
        if (result) {
            response = new ResponseEntity<Object>(HttpStatus.NO_CONTENT);
        } else {
            response = new ResponseEntity<Object>(HttpStatus.NOT_FOUND);
        }

        return response;
    }

    @RequestMapping(value = "/api/project/tool", method = RequestMethod.GET)
    public String getToolsList() {
        return service.getTools();
    }

    @RequestMapping(value = "/api/project/{name}/settings", method = RequestMethod.POST)
    public ResponseEntity<Object> updateProjectSettings(@PathVariable("name") String project, @RequestBody String settings) {
        boolean result = service.updateSettings(project, settings);
        if (result) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @RequestMapping(value = "/api/project/{name}/settings", method = RequestMethod.GET)
    public ResponseEntity<String> getProjectSettings(@PathVariable("name") String name) {
        String settings = service.getSettings(name);
        if (settings == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/project/{name}/report", method = RequestMethod.GET)
    public ResponseEntity<String> getProjectReport(@PathVariable("name") String name) {
        String report = service.getReport(name);
        if (report == null) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @RequestMapping(value = "/api/project/{name}/build", method = RequestMethod.GET)
    public String buildProject(@PathVariable("name") String name) {
        return service.buildProject(name);
    }
}