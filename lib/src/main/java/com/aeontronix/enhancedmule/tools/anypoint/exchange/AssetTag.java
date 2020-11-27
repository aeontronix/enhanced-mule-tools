/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.anypoint.exchange;

/**
 * Created by JacksonGenerator on 6/26/18.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class AssetTag {
    @JsonProperty("value")
    private String value;
    @JsonProperty("key")
    private String key;
    @JsonProperty("tagType")
    private String tagType;

    public AssetTag() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTagType() {
        return tagType;
    }

    public void setTagType(String tagType) {
        this.tagType = tagType;
    }
}
