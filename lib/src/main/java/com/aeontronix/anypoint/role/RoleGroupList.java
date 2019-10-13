/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.anypoint.role;

import com.aeontronix.anypoint.HttpException;
import com.aeontronix.anypoint.Organization;
import com.aeontronix.anypoint.util.JsonHelper;
import com.aeontronix.anypoint.util.PaginatedList;
import com.kloudtek.util.URLBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class RoleGroupList extends PaginatedList<RoleGroup, Organization> {
    private static final Logger logger = LoggerFactory.getLogger(RoleGroupList.class);

    public RoleGroupList(Organization org) throws HttpException {
        this(org, 50);
    }

    public RoleGroupList(Organization org, int limit) throws HttpException {
        super(org, limit);
        download();
    }

    @Override
    protected @NotNull URLBuilder buildUrl() {
        return new URLBuilder("/accounts/api/organizations/").path(parent.getId()).path("rolegroups").param("include_internal", false);
    }

    @Override
    protected void parseJson(String json, JsonHelper jsonHelper) {
        list = jsonHelper.readJsonList(RoleGroup.class, json, parent, "/data");
    }

    public List<RoleGroup> getRoleGroups() {
        return list;
    }

    public void setRoleGroups(List<RoleGroup> roleGroups) {
        list = roleGroups;
    }
}
