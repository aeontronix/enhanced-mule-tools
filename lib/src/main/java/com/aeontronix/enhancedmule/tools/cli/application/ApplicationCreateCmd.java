/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application;

import com.aeontronix.enhancedmule.tools.template.EMTProjectTemplate;
import com.aeontronix.enhancedmule.tools.util.VersionHelper;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.concurrent.Callable;

import static picocli.CommandLine.Help.Visibility.ALWAYS;

@Command(name = "create", aliases = "cr", mixinStandardHelpOptions = true)
public class ApplicationCreateCmd implements Callable<Integer> {
    @Parameters(arity = "1", description = "Project artifactId")
    private String artifactId;
    @Option(names = {"--dir", "-d"}, description = "directory where the project directory will be created")
    private File parentDir;
    @Option(names = {"--filename", "-f"}, description = "Name of the project directory")
    private String name;
    @Option(names = {"--groupId", "-g"}, description = "Maven group id", defaultValue = "com.company", showDefaultValue = ALWAYS)
    private String groupId;
    @Option(names = {"--project-name", "-n"}, description = "Project name")
    private String projectName;
    @Option(names = {"--runtime-version"}, description = "Mule runtime version", defaultValue = "4.3.0", showDefaultValue = ALWAYS)
    private String muleRuntimeVersion;
    @Option(names = {"--runtime-subversion"}, description = "Mule runtime sub-version", defaultValue = "20201013", showDefaultValue = ALWAYS)
    private String muleRuntimeSubVersion;
    @Option(names = {"--emt-version"}, description = "Enhance Mule Tools version", showDefaultValue = ALWAYS)
    private String emtVersion;
    @Option(names = {"--project-type","-t"}, description = "Project type", showDefaultValue = ALWAYS)
    private EMTProjectTemplate.ProjectType projectType = EMTProjectTemplate.ProjectType.REST;
    @Option(names = {"--domain","-m"}, description = "Use domain", negatable = true)
    private Boolean domain;
    @Option(names = {"--domain-group-id"}, description = "Use domain", showDefaultValue = ALWAYS)
    private String domainGroupId;
    @Option(names = {"--domain-artifact-id"}, description = "Domain artifact id")
    private String domainArtifactId;
    @Option(names = {"--domain-version"}, description = "Domain version")
    private String domainVersion;
    @Option(names = {"--api-spec-type"}, description = "REST API Specification Type")
    private EMTProjectTemplate.RestAPISpecType apiSpecType;

    public ApplicationCreateCmd() {
        emtVersion = VersionHelper.EMT_VERSION;
    }

    @Override
    public Integer call() throws Exception {
        final EMTProjectTemplate template = new EMTProjectTemplate(parentDir, name, groupId, artifactId, projectName,
                muleRuntimeVersion, emtVersion, projectType, apiSpecType, domain, domainArtifactId, domainVersion);
        template.generateProject();
        return 0;
    }
}
