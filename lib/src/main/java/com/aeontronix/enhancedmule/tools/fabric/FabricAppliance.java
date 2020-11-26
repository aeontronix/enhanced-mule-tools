package com.aeontronix.enhancedmule.tools.fabric;

/**
 * Created by JacksonGenerator on 10/31/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class FabricAppliance {
    @JsonProperty("version")
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
