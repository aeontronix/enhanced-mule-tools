package com.kloudtek.anypoint.role;

import com.kloudtek.anypoint.HttpException;
import com.kloudtek.anypoint.NotFoundException;
import com.kloudtek.anypoint.Organization;
import com.kloudtek.anypoint.util.JsonHelper;
import com.kloudtek.anypoint.util.PaginatedList;
import com.kloudtek.util.URLBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class RoleAssignmentList extends PaginatedList<RoleAssignment, Organization> {
    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentList.class);
    private final String roleId;

    public RoleAssignmentList(Organization org, String roleId) throws HttpException, NotFoundException {
        this(org, roleId, 50);
    }

    public RoleAssignmentList(Organization org, String roleId, int limit) throws HttpException, NotFoundException {
        super(org, limit);
        this.roleId = roleId;
        try {
            download();
        } catch (HttpException e) {
            if( e.getStatusCode() == 404 ) {
                throw new NotFoundException(e.getMessage(),e);
            }
            throw e;
        }
    }

    @Override
    public void download() throws HttpException {
        super.download();
    }

    @Override
    protected @NotNull URLBuilder buildUrl() {
        return new URLBuilder("/accounts/api/organizations/").path(parent.getId()).path("rolegroups/").path(roleId)
                .path("roles").param("include_internal",false);
    }

    @Override
    protected void parseJson(String json, JsonHelper jsonHelper) {
        list = jsonHelper.readJsonList(RoleAssignment.class, json, parent, "/data");
    }

    public List<RoleAssignment> getRoleGroups() {
        return list;
    }

    public void setRoleGroups(List<RoleAssignment> roleGroups) {
        list = roleGroups;
    }
}
