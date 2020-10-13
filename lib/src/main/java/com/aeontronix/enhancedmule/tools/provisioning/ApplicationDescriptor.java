/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.provisioning.api.*;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ApplicationDescriptor {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationDescriptor.class);
    private String id;
    private String version;
    private Boolean mule3;
    private APIDescriptor api;
    private HashMap<String, PropertyDescriptor> properties;
    private ClientApplicationDescriptor client;

    public ApplicationDescriptor() {
    }

    public ApplicationDescriptor(String name, String version) {
        api = new APIDescriptor(name, version);
    }

    public static ApplicationDescriptor read(APIProvisioningConfig apiProvisioningConfig, InputStream is) throws IOException {
        ObjectMapper mapper = JsonHelper.createMapper();
        String json = IOUtils.toString(is);
        String appId = (String) mapper.readValue(json, Map.class).get("id");
        if (appId != null) {
            apiProvisioningConfig.addVariable("app.id", appId);
        }
        json = StringUtils.substituteVariables(json, apiProvisioningConfig.getVariables());
        ApplicationDescriptor descriptor = mapper.readValue(json, ApplicationDescriptor.class);
        if (descriptor.getProperties() != null) {
            for (Map.Entry<String, PropertyDescriptor> entry : descriptor.getProperties().entrySet()) {
                entry.getValue().setName(entry.getKey());
            }
        }
        return descriptor;
    }

    public APIProvisioningResult provision(Environment environment, APIProvisioningConfig config, ApplicationSource source) throws ProvisioningException {
        try {
            APIProvisioningResult result = new APIProvisioningResult();
            if (api != null) {
                logger.debug("API descriptor found, provisioning");
                api.provision(this, environment, config, source, result);
            }
            if (client != null) {
                client.provision(this, environment, config, result);
            }
            return result;
        } catch (Exception e) {
            throw new ProvisioningException(e);
        }
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

    public HashMap<String, PropertyDescriptor> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, PropertyDescriptor> properties) {
        this.properties = properties;
    }

    public void addProperty(String key, boolean secure) {
        if (properties == null) {
            properties = new HashMap<>();
        }
        properties.put(key, new PropertyDescriptor(key, secure));
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
}
