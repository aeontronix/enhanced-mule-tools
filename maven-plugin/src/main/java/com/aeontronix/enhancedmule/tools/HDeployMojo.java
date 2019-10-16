/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.api.provision.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.api.provision.ProvisioningException;
import com.aeontronix.enhancedmule.tools.deploy.HDeploymentRequest;
import com.aeontronix.enhancedmule.tools.runtime.DeploymentResult;
import com.aeontronix.enhancedmule.tools.runtime.Server;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.Map;

/**
 * Deploy application to on-premise runtimes (hydrid model) via anypoint
 */
@Mojo(name = "hdeploy", requiresProject = false)
public class HDeployMojo extends AbstractDeployMojo {
    /**
     * Anypoint target name (Server / Server Group / Cluster)
     */
    @Parameter(name = "target", property = "anypoint.target", required = true)
    private String target;
    /**
     * Properties to be injected into the archive (properties resulting from API provisioning will be included in those
     * properties)
     */
    @Parameter(property = "anypoint.deploy.properties", required = false)
    protected Map<String, String> properties;
    /**
     * Name of file which will contain injected properties
     */
    @Parameter(property = "anypoint.deploy.retrydelay", required = false)
    protected String propertiesFilename = "deployconfig.properties";

    @SuppressWarnings("Duplicates")
    @Override
    protected DeploymentResult deploy(Environment environment, APIProvisioningConfig apiProvisioningConfig) throws Exception {
        try {
            Server server = environment.findServerByName(target);
            return new HDeploymentRequest(server, appName, source, filename, properties, apiProvisioningConfig).deploy();
        } catch (NotFoundException e) {
            throw new MojoExecutionException("Target " + target + " not found in env " + environment + " in business group " + org);
        } catch (ProvisioningException | IOException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}
