/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningRequest;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.deployment.DeploymentParameters;
import com.aeontronix.enhancedmule.tools.runtime.CHApplication;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static com.aeontronix.enhancedmule.propertiesprovider.utils.JacksonFlattener.flattenToStringMap;
import static java.lang.Boolean.TRUE;
import static org.slf4j.LoggerFactory.getLogger;

public class RuntimeDeploymentRequest extends AbstractDeploymentRequest implements ProvisioningRequest {
    private static final Logger logger = getLogger(RuntimeDeploymentRequest.class);
    private ApplicationDescriptor applicationDescriptor;
    private String appName;
    private String artifactId;
    private Environment environment;
    private boolean injectEnvInfo;
    private String target;
    private String filename;
    private final HashMap<String, String> vars = new HashMap<>();
    private final HashMap<String, String> properties = new HashMap<>();
    private HashSet<String> overrideProperties = new HashSet<>();
    private HashSet<String> secureProperties = new HashSet<>();
    private Map<String, String> fileProperties;
    private String filePropertiesPath = "config.properties";
    private boolean filePropertiesSecure;
    private boolean skipWait;
    private boolean skipProvisioning;
    private JsonNode legacyAppDescriptor;
    private boolean deleteSnapshots;

    public RuntimeDeploymentRequest(String filename, String appName, String artifactId, String buildNumber,
                                    Map<String, String> vars, Map<String, String> properties, File propertyfile,
                                    boolean ignoreMissingPropertyFile, String target, Environment environment,
                                    boolean injectEnvInfo, boolean skipWait,
                                    boolean skipProvisioning, JsonNode legacyAppDescriptor) throws IOException {
        super(buildNumber);
        this.filename = filename;
        this.appName = appName;
        this.artifactId = artifactId;
        this.environment = environment;
        this.injectEnvInfo = injectEnvInfo;
        this.skipWait = skipWait;
        this.skipProvisioning = skipProvisioning;
        this.legacyAppDescriptor = legacyAppDescriptor;
        if (vars != null && !vars.isEmpty()) {
            this.vars.putAll(vars);
        }
        this.properties.putAll(buildProperties(properties, propertyfile, ignoreMissingPropertyFile, injectEnvInfo));
        this.target = target;
        this.vars.put("environment.id", environment.getId());
        this.vars.put("environment.name", environment.getName());
        this.vars.put("environment.lname", environment.getLName());
        this.vars.put("environment.suffix", environment.getSuffix());
        this.vars.put("environment.type", environment.getType() != null ? environment.getType().name() : null);
        this.vars.put("organization.name", environment.getOrganization().getName());
        this.vars.put("organization.lname", environment.getOrganization().getName().replace(" ", "_").toLowerCase());
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> buildProperties(Map<String, String> properties, File propertyFile, boolean ignoreMissingPropertyFile,
                                                boolean injectEnvInfo) throws IOException {
        if (properties == null) {
            properties = new HashMap<>();
        }
        if (propertyFile != null) {
            if (propertyFile.exists()) {
                String propFilePath = propertyFile.getPath().toLowerCase();
                if (propFilePath.endsWith(".properties")) {
                    Properties fileProps = new Properties();
                    try (FileInputStream fis = new FileInputStream(propertyFile)) {
                        fileProps.load(fis);
                    }
                    for (Map.Entry<Object, Object> entry : fileProps.entrySet()) {
                        String key = entry.getKey().toString();
                        if (!properties.containsKey(key)) {
                            properties.put(key, entry.getValue().toString());
                        }
                    }
                } else if (propFilePath.endsWith(".json") || propFilePath.endsWith(".yaml")) {
                    ObjectMapper objectMapper;
                    if (propFilePath.endsWith(".json")) {
                        objectMapper = new ObjectMapper();
                    } else {
                        objectMapper = new ObjectMapper(new YAMLFactory());
                    }
                    Map fileProperties;
                    try {
                        fileProperties = objectMapper.readValue(propertyFile, Map.class);
                    } catch (Exception e) {
                        throw new IOException("An error occurred while reading " + propFilePath + " : " + e.getMessage(), e);
                    }
                    properties.putAll(flattenToStringMap(fileProperties));
                }
            } else {
                if (!ignoreMissingPropertyFile) {
                    throw new IllegalArgumentException("Property file not found: " + propertyFile);
                }
            }
        }
        if (injectEnvInfo) {
            setProperty("anypoint.env.name", environment.getName());
            setProperty("anypoint.env.suffix", environment.getSuffix());
            setProperty("anypoint.env.id", environment.getId());
            setProperty("anypoint.env.type", environment.getType().name());
            setProperty("anypoint.org.name", environment.getOrganization().getName());
            setProperty("anypoint.org.id", environment.getOrganization().getId());
        }
        return properties;
    }

    public void mergeExistingProperties(CHApplication existingApp) {
        if (existingApp != null) {
            Map<String, String> props = existingApp.getProperties();
            final DeploymentParameters dp = getApplicationDescriptor().getDeploymentParams();
            final Boolean mergeExistingProperties = dp.getMergeExistingProperties();
            if ((mergeExistingProperties == null || mergeExistingProperties) && props != null) {
                for (Map.Entry<String, String> entry : props.entrySet()) {
                    String key = entry.getKey();
                    if ((!properties.containsKey(key) || TRUE.equals(dp.getMergeExistingPropertiesOverride())) &&
                            !overrideProperties.contains(key)) {
                        properties.put(key, entry.getValue());
                    }
                }
            }
        }
    }

    public HashMap<String, String> getVars() {
        return vars;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public String getAppName() {
        return appName;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public HashSet<String> getSecureProperties() {
        return secureProperties;
    }

    public void addProperties(Map<String, String> properties) {
        properties.putAll(properties);
    }

    public Map<String, String> getFileProperties() {
        return fileProperties;
    }

    public void setFileProperties(Map<String, String> fileProperties) {
        this.fileProperties = fileProperties;
    }

    public boolean isFilePropertiesSecure() {
        return filePropertiesSecure;
    }

    public void setFilePropertiesSecure(boolean filePropertiesSecure) {
        this.filePropertiesSecure = filePropertiesSecure;
    }

    public void setOverrideProperty(String key, String value) {
        properties.put(key, value);
        overrideProperties.add(key);
    }

    public void setSecureOverrideProperty(String key, String value) {
        setOverrideProperty(key, value);
        secureProperties.add(key);
    }

    public void addFileProperty(String key, String value) {
        if (key == null) {
            throw new IllegalArgumentException("Property key musn't be null. value=" + value);
        }
        if (value == null) {
            throw new IllegalArgumentException("Property value musn't be null: " + key);
        }
        fileProperties.put(key, value);
    }

    public String getFilePropertiesPath() {
        return filePropertiesPath;
    }

    public void setFilePropertiesPath(String filePropertiesPath) {
        this.filePropertiesPath = filePropertiesPath;
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }

    public void setSecureProperty(String key) {
        secureProperties.add(key);
    }

    public void setSecureProperty(String key, String value) {
        properties.put(key, value);
        setSecureProperty(key);
    }

    public void addSecureProperties(Collection<String> secureProperties) {
        this.secureProperties.addAll(secureProperties);
    }

    public boolean isSkipWait() {
        return skipWait;
    }

    public void setSkipWait(boolean skipWait) {
        this.skipWait = skipWait;
    }

    public boolean isSkipProvisioning() {
        return skipProvisioning;
    }

    public void setSkipProvisioning(boolean skipProvisioning) {
        this.skipProvisioning = skipProvisioning;
    }

    public boolean isDeleteSnapshots() {
        return deleteSnapshots;
    }

    public void setDeleteSnapshots(boolean deleteSnapshots) {
        this.deleteSnapshots = deleteSnapshots;
    }

    @Override
    public ApplicationDescriptor getApplicationDescriptor() {
        return applicationDescriptor;
    }

    public void setApplicationDescriptor(ApplicationDescriptor applicationDescriptor) {
        this.applicationDescriptor = applicationDescriptor;
    }

    @Override
    public boolean isAutoApproveAPIAccessRequest() {
        final Boolean autoApproveAccess = applicationDescriptor.getDeploymentParams().getAutoApproveAccess();
        return autoApproveAccess == null || autoApproveAccess;
    }

    public JsonNode getLegacyOverrides() {
        return legacyAppDescriptor;
    }

}
