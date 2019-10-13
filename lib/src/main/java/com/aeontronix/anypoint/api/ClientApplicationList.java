/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.anypoint.api;

import com.aeontronix.anypoint.HttpException;
import com.aeontronix.anypoint.Organization;
import com.aeontronix.anypoint.util.PaginatedList;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kloudtek.util.URLBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientApplicationList extends PaginatedList<ClientApplication, Organization> {
    private final String filter;

    public ClientApplicationList(Organization organization, String filter) throws HttpException {
        super(organization);
        this.filter = filter;
        download();
    }

    @NotNull
    @Override
    protected URLBuilder buildUrl() {
        URLBuilder urlBuilder = new URLBuilder(parent.getUriPath() + "/applications").param("targetAdminSite", "true")
                .param("ascending", "true");
        if (filter != null) {
            urlBuilder.param("query", filter);
        }
        return urlBuilder;
    }

    @JsonProperty
    public List<ClientApplication> getApplications() {
        return list;
    }

    public void setApplications(List<ClientApplication> applications) {
        this.list = applications;
    }
}
