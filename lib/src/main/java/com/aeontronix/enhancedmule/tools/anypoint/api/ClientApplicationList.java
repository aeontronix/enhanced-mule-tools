/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.anypoint.api;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.PaginatedList;
import com.fasterxml.jackson.annotation.JsonProperty;
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
        URLBuilder urlBuilder = new URLBuilder(parent.getUriPath() + "/applications")
                .queryParam("ascending", "true");
        if (filter != null) {
            urlBuilder.queryParam("query", filter);
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
