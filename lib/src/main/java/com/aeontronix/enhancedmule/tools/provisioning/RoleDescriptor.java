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
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

import static com.aeontronix.enhancedmule.tools.util.MarkdownHelper.writeHeader;
import static com.aeontronix.enhancedmule.tools.util.MarkdownHelper.writeParagraph;

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

    @NotNull
    public Set<String> getExternalNames() {
        if( externalNames == null ) {
            externalNames = new HashSet<>();
        }
        return externalNames;
    }

    public void setExternalNames(@NotNull Set<String> externalNames) {
        this.externalNames = externalNames;
    }

    @NotNull
    public List<RolePermissionDescriptor> getPermissions() {
        if( permissions == null ) {
            permissions = new ArrayList<>();
        }
        return permissions;
    }

    public void setPermissions(@NotNull List<RolePermissionDescriptor> permissions) {
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
                        List<ProvisioningScope> scopes = permission.getScopes();
                        if( scopes != null && ! scopes.isEmpty() ) {
                            ArrayList<Environment> envs = new ArrayList<>(roleGroup.getParent().findAllEnvironments());
                            for (ProvisioningScope scope : scopes) {
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

    public void toMarkdown(Writer w, int headingDepth) throws IOException {
        writeHeader(w, headingDepth + 2, getName());
        if (StringUtils.isNotEmpty(getDescription())) {
            writeParagraph(w, "Description: " + getDescription());
        }
        if (!getExternalNames().isEmpty()) {
            writeParagraph(w, "Mapped to SSO Roles: " + String.join(", ", getExternalNames()));
        }
        if (!getPermissions().isEmpty()) {
            writeParagraph(w, "Permissions:");
        }
        for (RolePermissionDescriptor permission : getPermissions()) {
            StringBuilder perms = new StringBuilder();
            perms.append("- ").append(permission.getName());
            if (!permission.getScopes().isEmpty()) {
                List<String> scopes = permission.getScopes().stream().map(ProvisioningScope::toShortMarkdown)
                        .collect(Collectors.toList());
                perms.append(" for environment");
                if (scopes.size() > 1) {
                    perms.append("s");
                }
                perms.append(": ").append(String.join(", ", scopes));
            }
            writeParagraph(w, perms.toString());
        }
    }
}
