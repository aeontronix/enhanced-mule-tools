/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment.DeploymentParameters;
import com.aeontronix.enhancedmule.tools.runtime.CHApplication;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DeploymentConfig {
    private HashSet<String> overrideProperties = new HashSet<>();
    protected Map<String, String> fileProperties;
    protected String filePropertiesPath = "config.properties";
    protected boolean filePropertiesSecure;
    private DeploymentParameters deploymentParameters;

    public DeploymentConfig() {
    }

    @NotNull
    public DeploymentParameters getDeploymentParameters() {
        if (deploymentParameters == null) {
            deploymentParameters = new DeploymentParameters();
        }
        return deploymentParameters;
    }

    public void setDeploymentParameters(@NotNull DeploymentParameters deploymentParameters) {
        this.deploymentParameters = deploymentParameters;
    }

//    public void mergeExistingProperties(CHApplication existingApp, @NotNull ApplicationDescriptor applicationDescriptor) {
//        if (existingApp != null) {
//            Map<String, String> props = existingApp.getProperties();
//            final DeploymentParameters dp = getDeploymentParameters();
//            if (dp.isMergeExistingProperties() && props != null) {
//                for (Map.Entry<String, String> entry : props.entrySet()) {
//                    String key = entry.getKey();
//                    if ((!properties.containsKey(key) || dp.isMergeExistingPropertiesOverride()) && !overrideProperties.contains(key)) {
//                        properties.put(key, entry.getValue());
//                    }
//                }
//            }
//        }
//    }

//    public void setOverrideProperty(String key, String value) {
//        if (properties == null) {
//            properties = new HashMap<>();
//        }
//        properties.put(key, value);
//        overrideProperties.add(key);
//    }

    public Map<String, String> getFileProperties() {
        return fileProperties != null ? Collections.unmodifiableMap(fileProperties) : null;
    }

    public void setFileProperties(Map<String, String> fileProperties) {
        this.fileProperties = fileProperties;
        for (String key : fileProperties.keySet()) {
            if (key == null) {
                throw new IllegalArgumentException("file properties contains a null key.");
            }
        }
    }

    public String getFilePropertiesPath() {
        return filePropertiesPath;
    }

    public void setFilePropertiesPath(String filePropertiesPath) {
        this.filePropertiesPath = filePropertiesPath;
    }

    public boolean isFilePropertiesSecure() {
        return filePropertiesSecure;
    }

    public void setFilePropertiesSecure(boolean filePropertiesSecure) {
        this.filePropertiesSecure = filePropertiesSecure;
    }

    public void addFileProperty(String key, String value) {
        if (fileProperties == null) {
            fileProperties = new HashMap<>();
        }
        if (key == null) {
            throw new IllegalArgumentException("Property key musn't be null. value=" + value);
        }
        if (value == null) {
            throw new IllegalArgumentException("Property value musn't be null: " + key);
        }
        fileProperties.put(key, value);
    }
//
//    public void setProperty(String key, String value) {
//        if (properties == null) {
//            properties = new HashMap<>();
//        }
//        properties.put(key, value);
//    }
}
