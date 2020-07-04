/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.role;

import com.aeontronix.enhancedmule.tools.NotFoundException;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.aeontronix.enhancedmule.tools.util.PaginatedList;
import com.kloudtek.util.URLBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RoleAssignmentList extends PaginatedList<RoleAssignment, RoleGroup> {
    private static final Logger logger = LoggerFactory.getLogger(RoleAssignmentList.class);

    public RoleAssignmentList(RoleGroup rg) throws HttpException, NotFoundException {
        this(rg, 50);
    }

    public RoleAssignmentList(RoleGroup rg, int limit) throws HttpException, NotFoundException {
        super(rg, limit);
        try {
            download();
        } catch (HttpException e) {
            if (e.getStatusCode() == 404) {
                throw new NotFoundException(e.getMessage(), e);
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
        return new URLBuilder("/accounts/api/organizations/").path(parent.getParent().getId()).path("rolegroups/").path(parent.getId())
                .path("roles").param("include_internal", false);
    }

    @Override
    protected void parseJson(String json, JsonHelper jsonHelper) {
        list = jsonHelper.readJsonList(RoleAssignment.class, json, parent, "/data");
    }
}
