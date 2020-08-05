/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.role;

import com.aeontronix.enhancedmule.tools.Environment;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RoleAssignmentAddition {
    private final Map<String, String> contextParams = new HashMap<>();
    private String roleId;

    public RoleAssignmentAddition(String roleId, Environment environment) {
        this.roleId = roleId;
        if (environment != null) {
            contextParams.put("org", environment.getParent().getId());
            contextParams.put("envId", environment.getId());
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

    @Override
    public String toString() {
        return "RoleAssignmentAddition{" +
                "contextParams=" + contextParams +
                ", roleId='" + roleId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RoleAssignmentAddition that = (RoleAssignmentAddition) o;
        return Objects.equals(contextParams, that.contextParams) &&
                Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextParams, roleId);
    }
}
