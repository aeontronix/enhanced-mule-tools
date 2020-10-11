/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.exchange;

/**
 * Created by JacksonGenerator on 10/10/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class PagesItem {
    @JsonProperty("path")
    private String path;
    @JsonProperty("name")
    private String name;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
