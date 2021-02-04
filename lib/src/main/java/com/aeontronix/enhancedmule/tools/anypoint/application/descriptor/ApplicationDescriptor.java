/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.descriptor;

import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.api.ClientApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.api.PropertyDescriptor;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment.DeploymentParameters;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;

public class ApplicationDescriptor {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationDescriptor.class);
    private String id;
    private String name;
    private String description;
    private String version;
    private Boolean mule3;
    private APIDescriptor api;
    private HashMap<String, PropertyDescriptor> properties;
    private ClientApplicationDescriptor client;
    private DeploymentParameters deploymentParams;

    public ApplicationDescriptor() {
    }

    public static File findAnypointFile(File basedir) {
        File file = new File(basedir, "anypoint.yml");
        if (file.exists()) {
            return file;
        }
        file = new File(basedir, "anypoint.yaml");
        if (file.exists()) {
            return file;
        }
        file = new File(basedir, "anypoint.json");
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public static ApplicationDescriptor createDefault() {
        final ApplicationDescriptor applicationDescriptor = new ApplicationDescriptor();
        applicationDescriptor.setDeploymentParams(DeploymentParameters.createDefault());
        return applicationDescriptor;
    }

    public static JsonNode createDefault(ObjectMapper objectMapper) {
        return objectMapper.valueToTree(createDefault());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getMule3() {
        return mule3;
    }

    public void setMule3(Boolean mule3) {
        this.mule3 = mule3;
    }

    public APIDescriptor getApi() {
        return api;
    }

    public void setApi(APIDescriptor api) {
        this.api = api;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public synchronized HashMap<String, PropertyDescriptor> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }
        return properties;
    }

    public synchronized void setProperties(HashMap<String, PropertyDescriptor> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, boolean secure) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, new PropertyDescriptor(key, key, secure));
    }

    public ClientApplicationDescriptor getClient() {
        return client;
    }

    public void setClient(ClientApplicationDescriptor client) {
        this.client = client;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NotNull
    public DeploymentParameters getDeploymentParams() {
        if (deploymentParams == null) {
            deploymentParams = new DeploymentParameters();
        }
        return deploymentParams;
    }

    public void setDeploymentParams(DeploymentParameters deploymentParams) {
        this.deploymentParams = deploymentParams;
    }

    @JsonIgnore
    public boolean isAssetPublish() {
        if (api != null && api.getAsset() != null) {
            final Boolean create = api.getAsset().getCreate();
            return create != null && create;
        }
        return false;
    }
}
