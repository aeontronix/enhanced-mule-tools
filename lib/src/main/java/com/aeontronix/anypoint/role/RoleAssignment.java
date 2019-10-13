/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.anypoint.role;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

public class RoleAssignment {
    private String id;
    private String roleGroupId;
    private String roleId;
    private String orgId;
    private String name;
    private String description;
    private boolean internal;
    private Map<String, String> contextParams;

    @Override
    public String toString() {
        return new StringJoiner(", ", RoleAssignment.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("roleGroupId='" + roleGroupId + "'")
                .add("roleId='" + roleId + "'")
                .add("orgId='" + orgId + "'")
                .add("name='" + name + "'")
                .add("description='" + description + "'")
                .add("internal=" + internal)
                .add("contextParams=" + contextParams)
                .toString();
    }

    @JsonProperty("role_group_assignment_id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("role_group_id")
    public String getRoleGroupId() {
        return roleGroupId;
    }

    public void setRoleGroupId(String roleGroupId) {
        this.roleGroupId = roleGroupId;
    }

    @JsonProperty("role_id")
    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    @JsonProperty("org_id")
    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty
    public boolean isInternal() {
        return internal;
    }

    public void setInternal(boolean internal) {
        this.internal = internal;
    }

    @JsonProperty("context_params")
    @NotNull
    public Map<String, String> getContextParams() {
        if (contextParams == null) {
            contextParams = new HashMap<>();
        }
        return contextParams;
    }

    public void setContextParams(Map<String, String> contextParams) {
        this.contextParams = contextParams;
    }

    @Nullable
    public static RoleAssignment findByRoleIdAndEnvironmentId(@NotNull Collection<RoleAssignment> collection, @NotNull String roleId, @Nullable String environmentId) {
        for (RoleAssignment roleAssignment : collection) {
            if (roleAssignment.getRoleId().equals(roleId)) {
                if (environmentId != null) {
                    if (environmentId.equals(roleAssignment.getContextParams().get("envId"))) {
                        return roleAssignment;
                    }
                } else {
                    return roleAssignment;
                }
            }
        }
        return null;
    }
}
