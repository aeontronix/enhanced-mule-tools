/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy.rtf;

/**
 * Created by JacksonGenerator on 11/25/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class MuleAgentApplicationPropertiesService {
    @JsonProperty("applicationName")
    private String applicationName;
    @JsonProperty("properties")
    private Properties properties;
}
