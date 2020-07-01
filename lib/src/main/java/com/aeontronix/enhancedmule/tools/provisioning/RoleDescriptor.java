/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning;

import com.aeontronix.enhancedmule.tools.Organization;
import com.aeontronix.enhancedmule.tools.role.RoleGroup;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kloudtek.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RoleDescriptor {
    private String name;
    private String description;
    private Set<String> externalNames;
    private RolePermissionDescriptor permissions;

    @JsonProperty(required = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getExternalNames() {
        return externalNames;
    }

    public void setExternalNames(Set<String> externalNames) {
        this.externalNames = externalNames;
    }

    public RolePermissionDescriptor getPermissions() {
        return permissions;
    }

    public void setPermissions(RolePermissionDescriptor permissions) {
        this.permissions = permissions;
    }

    public void provision(Organization org) throws ProvisioningException, HttpException {
        if(StringUtils.isBlank(name)) {
            throw new ProvisioningException("role name missing");
        }
        List<RoleGroup> roleGroups = org.findAllRoleGroups().getAll();
        Optional<RoleGroup> roleGroupOpt = roleGroups.stream().filter(roleGroup -> roleGroup.getName().equalsIgnoreCase(name)).findFirst();
        RoleGroup roleGroup;
        if( ! roleGroupOpt.isPresent() ) {
            roleGroup = org.createRoleGroup(name, description);
        } else {
            roleGroup = roleGroupOpt.get();
            if( ! description.equals(roleGroup.getDescription()) ) {
                roleGroup.setDescription(description);
                roleGroup.update();
            }
        }
    }
}
