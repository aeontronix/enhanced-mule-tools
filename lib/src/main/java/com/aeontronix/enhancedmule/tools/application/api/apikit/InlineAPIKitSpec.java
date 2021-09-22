/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application.api.apikit;

public class InlineAPIKitSpec extends APIKitSpec {
    public String location;
    private String specAssetId;

    public InlineAPIKitSpec(String name, String location, String specAssetId) {
        super(name);
        this.location = location;
        this.specAssetId = specAssetId;
    }

    @Override
    public String getGroupId() {
        return null;
    }

    @Override
    public String getAssetId() {
        return specAssetId;
    }

    public String getLocation() {
        return location;
    }
}
