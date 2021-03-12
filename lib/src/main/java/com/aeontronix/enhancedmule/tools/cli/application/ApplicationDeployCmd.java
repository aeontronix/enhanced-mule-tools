/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.DeploymentServiceImpl;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;
import static picocli.CommandLine.*;

@Command(name = "deploy")
public class ApplicationDeployCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ApplicationDeployCmd.class);
    @Option(names = {"?", "-h", "--help"}, usageHelp = true, description = "display this help message")
    boolean usageHelpRequested;
    @ParentCommand
    private ApplicationCmd applicationCmd;
    @Parameters
    private String source;
    @Option(names = {"--target","-t"})
    private String target;
    @Option(names = {"--organization","-o"})
    private String organizationName;
    @Option(names = {"--environment","-e"})
    private String environmentName;
    @Option(names = {"--var","-V"})
    private HashMap<String,String> vars;
    @Option(names = {"--property","-P"})
    private HashMap<String,String> properties;
    @Option(names = {"--property-file","--pf"})
    private File propertyFile;
    @Option(names = {"--build-number","--bn"})
    private String buildNumber;
    @Option(names = {"--skip-wait","--sw"})
    private boolean skipWait;
    @Option(names = {"--skip-provisioning","--sp"})
    private boolean skipProvisioning;

    @Override
    public Integer call() throws Exception {
        Environment env = applicationCmd.getCli().getEnvironment(organizationName,environmentName);
        final DeploymentServiceImpl deploymentService = new DeploymentServiceImpl(env.getOrganization().getClient());
//        final RuntimeDeploymentRequest request = new RuntimeDeploymentRequest(filename != null ? filename :
//                source.getFileName(), appName, source.getArtifactId(), buildNumber, vars, properties, propertyfile,
//                target, getEnvironment(), injectEnvInfo, skipWait, skipProvisioning);

        return 0;
    }
}
