/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning;

import com.aeontronix.enhancedmule.tools.Environment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RolePermissionDescriptor {
    private String id;
    private String roleId;
    private String name;
    private List<ProvisioningScope> scopes;

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

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    @NotNull
    public List<ProvisioningScope> getScopes() {
        if( scopes == null ) {
            scopes = new ArrayList<>();
        }
        return scopes;
    }

    public void setScopes(@NotNull List<ProvisioningScope> scopes) {
        this.scopes = scopes;
    }

    public void addScope(ProvisioningScope scope) {
        if (scopes == null) {
            scopes = new ArrayList<>();
        }
        scopes.add(scope);
    }

    public List<String> getEnvironments() {
        return null;
    }

    public void setEnvironments(List<String> environments) {
        if (environments != null) {
            for (String environment : environments) {
                addScope(new ProvisioningScope(ProvisioningScope.Type.ENV, environment));
            }
        }
    }

    public synchronized void addEnvironment(String environment) {
        addScope(new ProvisioningScope(ProvisioningScope.Type.ENV, environment));
    }

    public List<Environment.Type> getEnvType() {
        return null;
    }

    public void setEnvType(List<Environment.Type> types) {
        if( types != null ) {
            for (Environment.Type envType : types) {
                addScope(new ProvisioningScope(ProvisioningScope.Type.ENV_TYPE, envType.name()));
            }
        }
    }
}
