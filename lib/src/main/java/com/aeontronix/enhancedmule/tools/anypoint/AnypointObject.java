/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.anypoint;

import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.annotation.JacksonInject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.Serializable;

import static org.slf4j.LoggerFactory.getLogger;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AnypointObject<X extends AnypointObject> implements Serializable {
    private static final Logger logger = getLogger(AnypointObject.class);
    @JsonIgnore
    protected String json;
    @JacksonInject()
    @JsonIgnore
    protected LegacyAnypointClient client;
    @JacksonInject()
    @JsonIgnore
    protected HttpHelper httpHelper;
    @JacksonInject()
    @JsonIgnore
    protected JsonHelper jsonHelper;
    @JsonIgnore
    protected X parent;

    public AnypointObject(LegacyAnypointClient client) {
        setClient(client);
    }

    public AnypointObject(X parent) {
        setParent(parent);
    }

    public AnypointObject() {
    }

    @JsonIgnore
    public LegacyAnypointClient getClient() {
        return client;
    }

    public void setParent(X parent) {
        this.parent = parent;
        setClient(parent.getClient());
    }

    public void setClient(LegacyAnypointClient client) {
        setClient(client, false);
    }

    public void setClient(@NotNull LegacyAnypointClient client, boolean setParent) {
        this.client = client;
        httpHelper = client.getHttpHelper();
        jsonHelper = client.getJsonHelper();
        if (logger.isDebugEnabled()) {
            logger.debug("Client assigned: " + client);
            logger.debug("httpHelper: " + httpHelper);
            logger.debug("jsonHelper: " + jsonHelper);
        }
        assert httpHelper != null && jsonHelper != null && this.client != null;
        if (setParent && parent != null) {
            parent.setClient(client);
        }
    }

    @JsonIgnore
    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }

    public X getParent() {
        return parent;
    }
}
