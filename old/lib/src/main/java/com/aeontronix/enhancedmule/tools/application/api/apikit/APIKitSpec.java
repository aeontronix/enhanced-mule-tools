/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application.api.apikit;

public abstract class APIKitSpec {
    public static final String RESOURCE_MARKER = "resource::";
    private String name;

    public APIKitSpec(String name) {
        this.name = name;
    }

    public static APIKitSpec create(String name, String api, String applicationArtifactId) {
        if( api.startsWith(RESOURCE_MARKER) ) {
            final String[] split = api.substring(RESOURCE_MARKER.length()).split(":");
            return new DependencyAPIKitSpec(name,split[0],split[1],split[2],split[3],split[4],split[5]);
        } else {
            return new InlineAPIKitSpec(name,api,applicationArtifactId+"-spec");
        }
    }

    public String getName() {
        return name;
    }

    public abstract String getGroupId();

    public abstract String getAssetId();
}

