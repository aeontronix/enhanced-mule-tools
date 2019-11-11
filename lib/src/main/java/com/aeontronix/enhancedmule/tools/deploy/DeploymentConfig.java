/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.deploy;

import com.aeontronix.enhancedmule.tools.runtime.CHApplication;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class DeploymentConfig {
    private boolean customlog4j;
    private boolean mergeExistingProperties = true;
    private boolean mergeExistingPropertiesOverride;
    private Map<String, String> properties = new HashMap<>();
    private HashSet<String> overrideProperties = new HashSet<>();
    protected Map<String, String> fileProperties;
    protected String filePropertiesPath = "config.properties";
    protected boolean filePropertiesSecure;

    public DeploymentConfig() {
    }

    public boolean isMergeExistingProperties() {
        return mergeExistingProperties;
    }

    public void setMergeExistingProperties(boolean mergeExistingProperties) {
        this.mergeExistingProperties = mergeExistingProperties;
    }

    public boolean isMergeExistingPropertiesOverride() {
        return mergeExistingPropertiesOverride;
    }

    public void setMergeExistingPropertiesOverride(boolean mergeExistingPropertiesOverride) {
        this.mergeExistingPropertiesOverride = mergeExistingPropertiesOverride;
    }

    public boolean isCustomlog4j() {
        return customlog4j;
    }

    public void setCustomlog4j(boolean customlog4j) {
        this.customlog4j = customlog4j;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void mergeExistingProperties(CHApplication existingApp) {
        if (existingApp != null) {
            Map<String, String> props = existingApp.getProperties();
            if (mergeExistingProperties && props != null) {
                for (Map.Entry<String, String> entry : props.entrySet()) {
                    String key = entry.getKey();
                    if ((!properties.containsKey(key) || mergeExistingPropertiesOverride) && !overrideProperties.contains(key)) {
                        properties.put(key, entry.getValue());
                    }
                }
            }
        }
    }

    public void setOverrideProperty(String key, String value) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, value);
        overrideProperties.add(key);
    }

    public Map<String, String> getFileProperties() {
        return fileProperties;
    }

    public void setFileProperties(Map<String, String> fileProperties) {
        this.fileProperties = fileProperties;
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
        fileProperties.put(key,value);
    }
}
