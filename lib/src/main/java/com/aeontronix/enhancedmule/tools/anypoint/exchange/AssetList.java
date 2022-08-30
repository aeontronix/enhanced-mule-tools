/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.anypoint.exchange;

import com.aeontronix.commons.URLBuilder;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.JsonHelper;
import com.aeontronix.enhancedmule.tools.util.PaginatedList;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AssetList extends PaginatedList<ExchangeAssetOverview, Organization> {
    private static final Logger logger = LoggerFactory.getLogger(AssetList.class);
    private final String filter;

    public AssetList(Organization org, String filter) throws HttpException {
        this(org, filter, 50);
    }

    public AssetList(Organization org, String filter, int limit) throws HttpException {
        super(org, limit);
        this.filter = filter;
        download();
    }

    @Override
    protected @NotNull URLBuilder buildUrl() {
        URLBuilder urlBuilder = new URLBuilder("/exchange/api/v1/assets")
                .queryParam("organizationId", parent.getId());
        if (filter != null) {
            urlBuilder.queryParam("search", filter);
        }
        return urlBuilder;
    }

    @Override
    protected void parseJson(String json, JsonHelper jsonHelper) {
        list = jsonHelper.readJsonList(ExchangeAssetOverview.class, json, parent);
    }

    public List<ExchangeAssetOverview> getAssets() {
        return list;
    }

    public void setAssets(List<ExchangeAssetOverview> assetOverviews) {
        list = assetOverviews;
    }

    public void delete() throws HttpException {
        for (ExchangeAssetOverview assetOverview : this) {
            for (AssetVersion assetVersion : assetOverview.getAsset().getVersions()) {
                assetVersion.delete();
            }
        }
    }
}
