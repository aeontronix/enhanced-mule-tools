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
    private String propertiesFilename;

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
                    if ( ( !properties.containsKey(key) || mergeExistingPropertiesOverride ) && !overrideProperties.contains(key)) {
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

    public String getPropertiesFilename() {
        return propertiesFilename;
    }

    public void setPropertiesFilename(String propertiesFilename) {
        this.propertiesFilename = propertiesFilename;
    }
}
