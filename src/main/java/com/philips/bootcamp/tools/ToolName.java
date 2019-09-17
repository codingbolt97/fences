package com.philips.bootcamp.tools;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.philips.bootcamp.domain.Tool;

public enum ToolName {
    MAVEN(new Maven()),
    CHECKSTYLE(new Checkstyle()),
    PMD(new PMD());

    @JsonIgnore
    private Tool toolInstance;

    private ToolName(Tool toolInstance) {
        this.toolInstance = toolInstance;
    }

    @JsonIgnore
    public Tool getInstance() {
        return toolInstance;
    }
}