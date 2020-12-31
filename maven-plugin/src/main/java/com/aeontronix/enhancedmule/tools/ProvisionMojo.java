/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.api.API;
import com.aeontronix.enhancedmule.tools.api.ClientApplication;
import com.aeontronix.enhancedmule.tools.legacy.deploy.Deployer;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationProvisioningService;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIProvisioningResult;
import com.aeontronix.enhancedmule.tools.provisioning.api.ClientApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Properties;

@Mojo(name = "provision", requiresProject = false, defaultPhase = LifecyclePhase.INSTALL)
public class ProvisionMojo extends AbstractEnvironmentalMojo {
    @Parameter(property = "anypoint.descriptor", required = false, defaultValue = "${project.build.directory}/anypoint.json")
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
    @Parameter(property = "provision.propsfile", required = false)
    private File propFile;
    @Parameter(property = "provision.includeplatcreds", required = false, defaultValue = "true")
    private boolean includePlatformCreds = true;

    @Override
    protected void doExecute() throws Exception {
        APIProvisioningConfig apiProvisioningConfig;
        if (!skipApiProvisioning) {
            if (!file.exists()) {
                throw new IllegalArgumentException("Descriptor file doesn't exist: " + file.getPath());
            }
            ApplicationDescriptor applicationDescriptor = getClient().getJsonHelper().getJsonMapper().readValue(file, ApplicationDescriptor.class);
            apiProvisioningConfig = new APIProvisioningConfig();
            Environment environment = getEnvironment();
            if (vars != null) {
                apiProvisioningConfig.setEnvironment(environment);
                apiProvisioningConfig.setVariables(vars);
            }
            getLog().info("Provisioning started");
            final ApplicationProvisioningService applicationProvisioningService = new ApplicationProvisioningService();
            APIProvisioningResult result = applicationProvisioningService.provision(applicationDescriptor,
                    environment, apiProvisioningConfig, null);
            getLog().info("Provisioning complete");
            Properties properties = new Properties();
            API api = result.getApi();
            final APIDescriptor apiDescriptor = applicationDescriptor.getApi();
            if (api != null && apiDescriptor.isInjectApiId()) {
                getLog().info(apiDescriptor.getApiIdProperty() + "=" + api.getId());
                properties.put(apiDescriptor.getApiIdProperty(), api.getId());
            }
            ClientApplication clientApplication = result.getClientApplication();
            final ClientApplicationDescriptor clientDescriptor = applicationDescriptor.getClient();
            if (clientApplication != null && clientDescriptor != null && clientDescriptor.isInjectClientIdSec()) {
                getLog().info(clientDescriptor.getClientIdProperty() + "=" + clientApplication.getClientId());
                properties.put(clientDescriptor.getClientIdProperty(), clientApplication.getClientId());
                properties.put(clientDescriptor.getClientSecretProperty(), clientApplication.getClientSecret());
            }
            String envClientId = environment.getClientId();
            if (includePlatformCreds) {
                getLog().info(Deployer.ANYPOINT_PLATFORM_CLIENT_ID + "=" + envClientId);
                properties.put(Deployer.ANYPOINT_PLATFORM_CLIENT_ID, envClientId);
                try {
                    properties.put(Deployer.ANYPOINT_PLATFORM_CLIENT_SECRET, environment.getClientSecret());
                } catch (HttpException e) {
                    if (e.getStatusCode() != 401) {
                        throw e;
                    }
                }
            }
            project.getProperties().putAll(properties);
            if (propFile != null) {
                Properties props = new Properties();
                if (propFile.exists()) {
                    try (FileReader in = new FileReader(propFile)) {
                        props.load(in);
                    }
                }
                props.putAll(properties);
                try (FileWriter out = new FileWriter(propFile)) {
                    props.store(out, "");
                }
            }
        }
    }
}
