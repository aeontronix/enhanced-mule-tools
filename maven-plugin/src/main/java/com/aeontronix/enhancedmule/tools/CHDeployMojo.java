/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.api.provision.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.deploy.CHDeploymentRequest;
import com.aeontronix.enhancedmule.tools.deploy.DeploymentConfig;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.util.Map;

/**
 * Deploy application to on-premise runtimes (hydrid model) via anypoint
 */
@Mojo(name = "cdeploy", requiresProject = false)
public class CHDeployMojo extends AbstractDeployMojo {
    /**
     * Mule version name (will default to latest if not set)
     */
    @Parameter(name = "muleVersionName", property = "anypoint.deploy.ch.muleversion", required = false)
    private String muleVersionName;
    /**
     * Deployment region
     */
    @Parameter(name = "region", property = "anypoint.deploy.ch.region", required = false)
    private String region;
    /**
     * Worker type (will default to smallest if not specified)
     */
    @Parameter(name = "workerType", property = "anypoint.deploy.ch.worker.type", required = false)
    private String workerType;
    /**
     * Worker count (will default to one if not specified)
     */
    @Parameter(name = "workerCount", property = "anypoint.deploy.ch.worker.count", required = false)
    private Integer workerCount;

    /**
     * Specifies is standard cloudhub logging should be disabled and the log4j configuration in the project be used instead.
     */
    @Parameter(name = "customlog4j", property = "anypoint.deploy.ch.customlog4j")
    private boolean customlog4j;

    /**
     * Application properties
     */
    @Parameter(property = "anypoint.deploy.properties", required = false)
    protected Map<String, String> properties;

    @SuppressWarnings("Duplicates")
    @Override
    protected DeploymentResult deploy(Environment environment, APIProvisioningConfig apiProvisioningConfig, DeploymentConfig deploymentConfig) throws Exception {
        if (workerCount == null) {
            workerCount = 1;
        }
        deploymentConfig.setCustomlog4j(customlog4j);
        return new CHDeploymentRequest(muleVersionName, region, workerType, workerCount, environment, appName, source,
                filename, apiProvisioningConfig, deploymentConfig).deploy();
    }
}