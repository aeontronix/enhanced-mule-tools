/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.legacy.deploy.CHDeployer;
import com.aeontronix.enhancedmule.tools.legacy.deploy.DeploymentConfig;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;

@Command(name = "chdeploy", description = "Deploy Application to cloudhub", showDefaultValues = true)
public class CHDeployApplicationCmd extends AbstractDeployApplicationCmd {
    private static final Logger logger = LoggerFactory.getLogger(CHDeployApplicationCmd.class);
    @Option(names = {"-mv", "--mule-version"}, description = "Mule runtime version (if not set, default will be used)")
    private String muleVersion;
    @Option(names = {"-rg", "--region"}, description = "Cloudhub region (if not set, default will be used)")
    private String region;
    @Option(names = {"-wt", "--worker-type"}, description = "Cloudhub region (if not set, it will default to the smallest worker type)")
    private String workerType;
    @Option(names = {"-wc", "--worker-count"}, description = "Number of workers to setup")
    private int workerCount = 1;

    @Override
    protected DeploymentResult deploy(Environment environment) throws ProvisioningException, IOException, HttpException, NotFoundException {
        DeploymentConfig deploymentConfig = new DeploymentConfig();
        CHDeployer req = new CHDeployer(muleVersion, region, workerType, workerCount, environment,
                appName, source, filename, apiProvisioningConfig, deploymentConfig);
        return req.deploy();
    }
}
