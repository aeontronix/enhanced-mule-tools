/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.api.provision;

import com.aeontronix.enhancedmule.tools.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class AnypointDescriptor {
    private static final Logger logger = LoggerFactory.getLogger(AnypointDescriptor.class);
    private String id;
    private Boolean mule3;
    private APIDescriptor api;
    private HashMap<String, PropertyDescriptor> properties;
    private ClientApplicationDescriptor client;

    public AnypointDescriptor() {
    }

    public AnypointDescriptor(String name, String version) {
        api = new APIDescriptor(name, version);
    }

    public APIProvisioningResult provision(Environment environment, APIProvisioningConfig config) throws ProvisioningException {
        try {
            config.setVariable("app.id", id);
            config.setVariable("environment.id", environment.getId());
            config.setVariable("environment.name", environment.getName());
            config.setVariable("environment.lname", environment.getName().replace(" ", "_").toLowerCase());
            config.setVariable("organization.name", environment.getOrganization().getName());
            config.setVariable("organization.lname", environment.getOrganization().getName().replace(" ", "_").toLowerCase());
            APIProvisioningResult result = new APIProvisioningResult();
            if (api != null) {
                logger.debug("API descriptor found, provisioning");
                api.provision(this, environment, config, result);
                if( client != null ) {
                    client.provision(this,environment,config, result);
                }
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
}
