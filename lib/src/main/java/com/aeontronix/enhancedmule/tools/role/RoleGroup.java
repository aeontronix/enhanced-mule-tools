/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.role;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointObject;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.provisioning.RoleDescriptor;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.UnexpectedException;

import java.util.*;

public class RoleGroup extends AnypointObject<Organization> {
    @JsonProperty("role_group_id")
    private String id;
    @JsonProperty
    private String name;
    @JsonProperty
    private String description;
    @JsonProperty
    private boolean editable;
    @JsonProperty("external_names")
    private Set<String> externalNames;

    public RoleGroup(AnypointClient client) {
        super(client);
    }

    public RoleGroup(Organization parent) {
        super(parent);
    }

    public RoleGroup() {
    }

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public Set<String> getExternalNames() {
        return externalNames;
    }

    public void setExternalNames(Set<String> externalNames) {
        this.externalNames = externalNames;
    }

    public RoleGroup update() throws HttpException {
        HashMap<String, Object> changes = new HashMap<>();
        if (editable) {
            changes.put("name", name);
            changes.put("description", description);
        }
        changes.put("external_names", externalNames);
        String json = httpHelper.httpPut(buildUrlStr(parent, id), changes);
        return jsonHelper.readJson(new RoleGroup(), json, parent);
    }

    public static RoleGroup findById(Organization organization, HttpHelper httpHelper, JsonHelper jsonHelper, String id) throws NotFoundException, HttpException {
        try {
            String buildUrl = buildUrlStr(organization, id);
            String json = httpHelper.httpGet(buildUrl);
            return jsonHelper.readJson(new RoleGroup(), json, organization);
        } catch (HttpException e) {
            if (e.getStatusCode() == 404) {
                throw new NotFoundException("Role with id " + id + " not found in org " + organization.getId());
            } else {
                throw e;
            }
        }
    }

    public RoleAssignmentList findRoleAssignments() throws HttpException, NotFoundException {
        try {
            return new RoleAssignmentList(this);
        } catch (HttpException e) {
            if (e.getStatusCode() == 404) {
                throw new NotFoundException(e.getMessage());
            } else {
                throw e;
            }
        }
    }

    public List<RoleAssignment> assignRoles(Iterable<RoleAssignmentAddition> roleAssignmentAdditions) throws HttpException {
        String json = httpHelper.httpPost(buildUrl(parent, id).path("roles").toString(), roleAssignmentAdditions);
        return jsonHelper.readJsonList(RoleAssignment.class, json, null);
    }

    public void deleteAllRoleAssignment() throws HttpException {
        try {
            deleteRoleAssignment(findRoleAssignments());
        } catch (NotFoundException e) {
            throw new UnexpectedException(e);
        }
    }

    public void deleteRoleAssignment(Iterable<RoleAssignment> roleAssignments) throws HttpException {
        for (RoleAssignment roleAssignment : roleAssignments) {
            httpHelper.httpDelete(buildUrl(parent, getId()).path("roles").param("roleId", roleAssignment.getId()).toString(),
                    Collections.singletonList(roleAssignment));
        }
    }

    private static String buildUrlStr(Organization organization, String id) {
        return buildUrl(organization, id).toString();
    }

    private static URLBuilder buildUrl(Organization organization, String id) {
        return new URLBuilder("/accounts/api/organizations/").path(organization.getId()).path("rolegroups").path(id);
    }

    public boolean same(RoleDescriptor roleDescriptor) {
        return Objects.equals(roleDescriptor.getDescription(),description) &&
                Objects.equals(roleDescriptor.getExternalNames(),externalNames);
    }
}
