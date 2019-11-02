/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.api.provision.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.api.provision.ProvisioningException;
import com.aeontronix.enhancedmule.tools.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.deploy.CHDeploymentRequest;
import com.aeontronix.enhancedmule.tools.deploy.DeploymentConfig;
import com.aeontronix.enhancedmule.tools.deploy.HDeploymentRequest;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import com.kloudtek.util.StringUtils;
import com.kloudtek.util.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.Map;

/**
 * Deploy an application to Cloudhub or On-Prem/Hybrid
 */
@Mojo(name = "deploy", requiresProject = false)
public class DeployMojo extends AbstractDeployMojo {
    /**
     * Anypoint target name (Server / Server Group / Cluster). If not set will deploy to Cloudhub
     */
    @Parameter(name = "target", property = "anypoint.target")
    private String target;

    /**
     * Properties to be injected into the archive (properties resulting from API provisioning will be included in those
     * properties)
     */
    @Parameter(property = "anypoint.deploy.properties", required = false)
    protected Map<String, String> properties;

    /**
     * Hybrid/Onprem only: Name of file which will contain injected properties
     */
    @Parameter(property = "anypoint.deploy.properties.file", required = false)
    protected String propertiesFilename = "deployconfig.properties";

    /**
     * Cloudhub only: Mule version name (will default to latest if not set)
     */
    @Parameter(name = "muleVersionName", property = "anypoint.deploy.ch.muleversion", required = false)
    private String muleVersionName;

    /**
     * Cloudhub only: Deployment region
     */
    @Parameter(name = "region", property = "anypoint.deploy.ch.region", required = false)
    private String region;

    /**
     * Cloudhub only: Worker type (will default to smallest if not specified)
     */
    @Parameter(name = "workerType", property = "anypoint.deploy.ch.worker.type", required = false)
    private String workerType;

    /**
     * Cloudhub only: Worker count (will default to one if not specified).
     */
    @Parameter(name = "workerCount", property = "anypoint.deploy.ch.worker.count")
    private Integer workerCount;

    /**
     * Cloudhub only: If true custom log4j will be used (and cloudhub logging disabled)
     */
    @Parameter(name = "customlog4j", property = "anypoint.deploy.ch.customlog4j")
    private boolean customlog4j;

    @SuppressWarnings("Duplicates")
    @Override
    protected DeploymentResult deploy(Environment environment, APIProvisioningConfig apiProvisioningConfig, DeploymentConfig deploymentConfig) throws Exception {
        ApplicationSource applicationSource = ApplicationSource.create(environment.getOrganization().getId(), environment.getClient(), file);
        try {
            if (StringUtils.isBlank(target)) {
                if (workerCount == null) {
                    workerCount = 1;
                }
                try {
                    deploymentConfig.setCustomlog4j(customlog4j);
                    return new CHDeploymentRequest(muleVersionName, region, workerType, workerCount, environment, appName,
                            applicationSource, filename, apiProvisioningConfig, deploymentConfig).deploy();
                } catch (ProvisioningException | IOException | NotFoundException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            } else {
                try {
                    Server server = environment.findServerByName(target);
                    return new HDeploymentRequest(server, appName, applicationSource, filename, properties,
                            apiProvisioningConfig, deploymentConfig).deploy();
                } catch (NotFoundException e) {
                    throw new MojoExecutionException("Target " + target + " not found in env " + environment + " in business group " + org);
                } catch (ProvisioningException | IOException e) {
                    throw new MojoExecutionException(e.getMessage(), e);
                }
            }
        } finally {
            IOUtils.close(applicationSource);
        }
    }
}
