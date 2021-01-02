/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.anypoint.api;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.PaginatedList;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.aeontronix.commons.URLBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class APIList extends PaginatedList<APIAsset, Environment> {
    private final String filter;

    public APIList(Environment environment, String filter) throws HttpException {
        this(environment, filter, 20);
    }

    public APIList(Environment environment, String filter, int limit) throws HttpException {
        super(environment, limit);
        this.filter = filter;
        download();
    }

    @Override
    protected @NotNull URLBuilder buildUrl() {
        URLBuilder urlBuilder = new URLBuilder("/apimanager/api/v1/organizations/" + parent.getParent().getId() + "/environments/" + parent.getId() + "/apis")
                .param("ascending", "true");
        if (filter != null) {
            urlBuilder.param("query", filter);
        }
        urlBuilder.param("sort", "createdDate");
        return urlBuilder;
    }

    @JsonProperty
    public List<APIAsset> getAssets() {
        return list;
    }

    public void setAssets(List<APIAsset> assets) {
        list = assets;
    }
}
