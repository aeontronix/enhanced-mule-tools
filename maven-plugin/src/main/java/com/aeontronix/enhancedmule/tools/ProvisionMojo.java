/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.api.API;
import com.aeontronix.enhancedmule.tools.api.ClientApplication;
import com.aeontronix.enhancedmule.tools.api.provision.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.api.provision.APIProvisioningResult;
import com.aeontronix.enhancedmule.tools.api.provision.AnypointDescriptor;
import com.aeontronix.enhancedmule.tools.deploy.DeploymentRequest;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

@Mojo(name = "provision", requiresProject = false, defaultPhase = LifecyclePhase.INSTALL)
public class ProvisionMojo extends AbstractEnvironmentalMojo {
    @Parameter(property = "anypoint.pdescriptor", required = false, defaultValue = "${project.build.directory}/anypoint.json")
    private File file;
    /**
     * Provisioning variables
     */
    @Parameter
    protected HashMap<String, String> vars;
    /**
     * If true API provisioning will be skipped
     */
    @Parameter(property = "anypoint.api.provisioning.skip")
    protected boolean skipApiProvisioning;
    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    protected void doExecute() throws Exception {
        APIProvisioningConfig apiProvisioningConfig;
        if (!skipApiProvisioning) {
            if (!file.exists()) {
                throw new IllegalArgumentException("Descriptor file doesn't exist: " + file.getPath());
            }
            AnypointDescriptor anypointDescriptor = getClient().getJsonHelper().getJsonMapper().readValue(file, AnypointDescriptor.class);
            apiProvisioningConfig = new APIProvisioningConfig();
            if (vars != null) {
                apiProvisioningConfig.setVariables(vars);
            }
            getLog().info("Provisioning started");
            APIProvisioningResult result = anypointDescriptor.provision(getEnvironment(), apiProvisioningConfig);
            getLog().info("Provisioning complete");
            Properties properties = project.getProperties();
            API api = result.getApi();
            if (api != null) {
                getLog().info(apiProvisioningConfig.getInjectApiIdKey()+"="+api.getId());
                properties.put(apiProvisioningConfig.getInjectApiIdKey(), api.getId());
            }
            ClientApplication clientApplication = result.getClientApplication();
            if (clientApplication != null) {
                getLog().info(apiProvisioningConfig.getInjectClientIdSecretKey()+"="+clientApplication.getClientId());
                properties.put(apiProvisioningConfig.getInjectClientIdSecretKey() + ".id", clientApplication.getClientId());
                properties.put(apiProvisioningConfig.getInjectClientIdSecretKey() + ".secret", clientApplication.getClientSecret());
            }
            String envClientId = getEnvironment().getClientId();
            getLog().info(DeploymentRequest.ANYPOINT_PLATFORM_CLIENT_ID+"="+envClientId);
            properties.put(DeploymentRequest.ANYPOINT_PLATFORM_CLIENT_ID, envClientId);
            try {
                properties.put(DeploymentRequest.ANYPOINT_PLATFORM_CLIENT_SECRET,getEnvironment().getClientSecret());
            } catch (HttpException e) {
                if (e.getStatusCode() != 401) {
                    throw e;
                }
            }
        }
    }
}
