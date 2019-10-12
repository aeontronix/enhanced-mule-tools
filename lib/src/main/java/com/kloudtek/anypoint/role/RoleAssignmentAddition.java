package com.kloudtek.anypoint.role;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kloudtek.anypoint.Environment;

import java.util.HashMap;
import java.util.Map;

public class RoleAssignmentAddition {
    private final Map<String,String> contextParams = new HashMap<>();
    private String roleId;

    public RoleAssignmentAddition(String roleId, Environment environment) {
        this.roleId = roleId;
        if( environment !=null ) {
            contextParams.put("org",environment.getParent().getId());
            contextParams.put("envId",environment.getId());
        }
    }

    @JsonProperty("role_id")
    public String getRoleId() {
        return roleId;
    }

    @JsonProperty("context_params")
    public Map<String, String> getContextParams() {
        return contextParams;
    }

    @JsonIgnore
    public String getEnvironmentId() {
        return contextParams.get("envId");
    }
}
