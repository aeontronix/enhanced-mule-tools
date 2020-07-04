/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning;

import com.aeontronix.enhancedmule.tools.Environment;
import com.aeontronix.enhancedmule.tools.NotFoundException;
import com.aeontronix.enhancedmule.tools.Organization;
import com.aeontronix.enhancedmule.tools.role.Role;
import com.aeontronix.enhancedmule.tools.role.RoleAssignmentAddition;
import com.aeontronix.enhancedmule.tools.role.RoleGroup;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kloudtek.util.StringUtils;

import java.util.*;

public class RoleDescriptor {
    private String id;
    private String name;
    private String description;
    private Set<String> externalNames;
    private List<RolePermissionDescriptor> permissions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public List<RolePermissionDescriptor> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<RolePermissionDescriptor> permissions) {
        this.permissions = permissions;
    }

    public void provision(Organization org) throws ProvisioningException, HttpException {
        try {
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
                if(!roleGroup.same(this)) {
                    roleGroup.setDescription(description);
                    roleGroup.setExternalNames(externalNames);
                    roleGroup.update();
                }
            }
            if( roleGroup.isEditable() ) {
                roleGroup.deleteAllRoleAssignment();
                if( permissions != null && ! permissions.isEmpty() ) {
                    Map<String, Role> roles = roleGroup.getParent().findAllRolesIndexedByName();
                    ArrayList<RoleAssignmentAddition> assignments = new ArrayList<>();
                    for (RolePermissionDescriptor permission : permissions) {
                        Role role = roles.get(permission.getName());
                        if( role == null ) {
                            throw new ProvisioningException("Role not found: "+permission.getName());
                        }
                        List<RolePermissionScope> scopes = permission.getScopes();
                        if( scopes != null && ! scopes.isEmpty() ) {
                            ArrayList<Environment> envs = new ArrayList<>(roleGroup.getParent().findAllEnvironments());
                            for (RolePermissionScope scope : scopes) {
                                for (Environment matchEnvironment : scope.matchEnvironments(envs)) {
                                    assignments.add(new RoleAssignmentAddition(role.getId(),matchEnvironment));
                                }
                            }
                        } else {
                            assignments.add(new RoleAssignmentAddition(role.getId(),null));
                        }
                    }
                    roleGroup.assignRoles(assignments);
                }
            }
        } catch (NotFoundException e) {
            throw new ProvisioningException(e);
        }
    }
}
