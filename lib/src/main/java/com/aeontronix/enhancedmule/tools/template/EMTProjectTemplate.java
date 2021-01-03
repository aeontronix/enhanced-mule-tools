/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.template;

import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import com.aeontronix.genesis.*;

import java.io.File;
import java.io.IOException;

public class EMTProjectTemplate {
    private File dir;
    private String filename;
    private final String groupId;
    private final String artifactId;
    private final String projectName;
    private final String muleRuntimeVersion;
    private String emtVersion;
    private final ProjectType projectType;
    private Boolean domain;
    private String domainGroupId;
    private String domainArtifactId;
    private String domainVersion;
    private final RestAPISpecType apiSpecType;

    public EMTProjectTemplate(File dir,
                              String filename,
                              String groupId,
                              String artifactId,
                              String projectName,
                              String muleRuntimeVersion, String emtVersion,
                              ProjectType projectType, RestAPISpecType apiSpecType,
                              Boolean domain, String domainArtifactId, String domainVersion) {
        this.dir = dir;
        this.filename = filename;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.projectName = projectName;
        this.muleRuntimeVersion = muleRuntimeVersion;
        this.emtVersion = emtVersion;
        this.projectType = projectType;
        this.domain = domain;
        this.domainArtifactId = domainArtifactId;
        this.domainVersion = domainVersion;
        if( this.emtVersion == null ) {
            this.emtVersion = VersionHelper.EMT_VERSION;
        }
        this.apiSpecType = apiSpecType;
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
        setVar(templateExecutor, "muleRuntimeVersion", muleRuntimeVersion);
        setVar(templateExecutor, "emtVersion", emtVersion);
        if( projectType != null ) {
            setVar(templateExecutor, "projectType", projectType.name().toLowerCase());
        }
        if( domain != null ) {
            setVar(templateExecutor, "domain", domain ? "true": "false");
        }
        setVar(templateExecutor, "domainGroupId", domainGroupId);
        setVar(templateExecutor, "domainArtifactId", domainArtifactId);
        setVar(templateExecutor, "domainVersion", domainVersion);
        if( apiSpecType != null ) {
            setVar(templateExecutor, "apiSpecType", apiSpecType.name().toLowerCase());
        }
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

    public void setDomain(String domainGroupId, String domainArtifactId, String domainVersion) {
        this.domain = true;
        this.domainGroupId = domainGroupId;
        this.domainArtifactId = domainArtifactId;
        this.domainVersion = domainVersion;
    }

    public enum ProjectType {
        MINIMAL, REST
    }

    public enum RestAPISpecType {
        RAML, OAS2JSON, OAS2YAML, OAS3JSON, OAS3YAML
    }
}
