/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application;

import com.aeontronix.enhancedmule.tools.template.EMTProjectTemplate;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "create", aliases = "cr", mixinStandardHelpOptions = true)
public class ApplicationCreateCmd implements Callable<Integer> {
    @Option(names = {"--parentdir", "-pd"}, description = "directory where the project directory will be created")
    private File parentDir;
    @Option(names = {"--name", "-n"}, description = "Name of the project directory")
    private String name;
    @Option(names = {"--groupId", "-g"}, description = "Maven group id")
    private String groupId;
    @Option(names = {"--artifactId", "-id"}, description = "Maven artifact id")
    private String artifactId;
    @Option(names = {"--projectName", "-pn"}, description = "Project name")
    private String projectName;

    @Override
    public Integer call() throws Exception {
        final EMTProjectTemplate template = new EMTProjectTemplate(parentDir, name, groupId, artifactId, projectName);
        template.generateProject();
        return 0;
    }
}
