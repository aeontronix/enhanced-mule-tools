/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.exchange;

/**
 * Created by JacksonGenerator on 6/26/18.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class AssetAttribute {
    @JsonProperty("tagType")
    private String tagType;
    @JsonProperty("value")
    private String value;
    @JsonProperty("key")
    private String key;
}
