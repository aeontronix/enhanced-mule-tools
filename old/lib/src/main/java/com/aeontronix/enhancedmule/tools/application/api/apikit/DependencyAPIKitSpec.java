/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application.api.apikit;

public class DependencyAPIKitSpec extends APIKitSpec {
    private String groupId;
    private String assetId;
    private String version;
    private String type;
    private String ext;
    private String path;

    public DependencyAPIKitSpec(String name, String groupId, String assetId, String version, String type, String ext, String path) {
        super(name);
        this.groupId = groupId;
        this.assetId = assetId;
        this.version = version;
        this.type = type;
        this.ext = ext;
        this.path = path;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getAssetId() {
        return assetId;
    }

    public String getVersion() {
        return version;
    }

    public String getType() {
        return type;
    }

    public String getExt() {
        return ext;
    }

    public String getPath() {
        return path;
    }
}
