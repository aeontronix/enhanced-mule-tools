/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.role;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

public class RoleAssignmentDeletion {
    private final Map<String, String> contextParams = new HashMap<>();
    private String roleGroupId;
    private String roleId;

    public RoleAssignmentDeletion(String roleGroupId, String roleId, Environment environment) {
        this.roleGroupId = roleGroupId;
        this.roleId = roleId;
        contextParams.put("org", environment.getParent().getId());
        contextParams.put("envId", environment.getId());
    }

    @JsonProperty("role_group_id")
    public String getRoleGroupId() {
        return roleGroupId;
    }

    @JsonProperty("role_id")
    public String getRoleId() {
        return roleId;
    }

    @JsonProperty("context_params")
    public Map<String, String> getContextParams() {
        return contextParams;
    }
}
