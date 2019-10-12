package com.kloudtek.anypoint.role;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Role {
    @JsonProperty("role_id")
    private String id;
    private String name;
    @JsonProperty("context_keys")
    private List<String> contextKeys;
    @JsonProperty("environment_ids")
    private List<String> environmentIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getContextKeys() {
        return contextKeys;
    }

    public void setContextKeys(List<String> contextKeys) {
        this.contextKeys = contextKeys;
    }

    public List<String> getEnvironmentIds() {
        return environmentIds;
    }

    public void setEnvironmentIds(List<String> environmentIds) {
        this.environmentIds = environmentIds;
    }
}
