/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.anypoint;

import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AnypointObject<X extends AnypointObject> implements Serializable {
    @JsonIgnore
    protected String json;
    @JsonIgnore
    protected LegacyAnypointClient client;
    @JsonIgnore
    protected HttpHelper httpHelper;
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

    public void setClient(LegacyAnypointClient client, boolean setParent) {
        this.client = client;
        httpHelper = client.getHttpHelper();
        jsonHelper = client.getJsonHelper();
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
