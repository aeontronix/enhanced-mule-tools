/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.exchange;

/**
 * Created by JacksonGenerator on 10/17/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AssetCategory {
    @JsonProperty("displayName")
    private String displayName;
    @JsonProperty("dataType")
    private String dataType;
    @JsonProperty("tagType")
    private String tagType;
    @JsonProperty("value")
    private List<String> value;
    @JsonProperty("key")
    private String key;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }

    public List<String> getValue() {
        return value;
    }

    public void setValue(List<String> value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
