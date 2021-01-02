/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.template;

import com.aeontronix.genesis.*;

import java.io.File;
import java.io.IOException;

public class EMTProjectTemplate {
    private File dir;
    private String filename;
    private String groupId;
    private String artifactId;
    private String projectName;

    public EMTProjectTemplate(File dir, String filename) {
        this.dir = dir;
        this.filename = filename;
    }

    public EMTProjectTemplate(File dir, String filename, String groupId, String artifactId, String projectName) {
        this.dir = dir;
        this.filename = filename;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.projectName = projectName;
    }

    public void generateProject() throws IOException, TemplateNotFoundException, InvalidTemplateException, TemplateExecutionException {
        if (dir == null) {
            dir = new File(".");
        }
        final Template template = Template.createFromClasspath("/template", "emt-genesis-template.json");
        final TemplateExecutor templateExecutor = new TemplateExecutor(template);
        setVar(templateExecutor, "groupId", groupId);
        setVar(templateExecutor, "artifactId", artifactId);
        setVar(templateExecutor, "projectName", projectName);
        templateExecutor.runSteps();
        if (filename == null) {
            filename = templateExecutor.getVariable("artifactId");
        }
        templateExecutor.generate(new File(dir, filename));
    }

    private void setVar(TemplateExecutor templateExecutor, String key, String value) {
        if (value != null) {
            templateExecutor.setVariable(key, value);
        }
    }
}
