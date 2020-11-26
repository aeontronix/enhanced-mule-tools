package com.aeontronix.enhancedmule.tools.fabric;

/**
 * Created by JacksonGenerator on 10/31/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class FabricFeatures {
    @JsonProperty("enhancedSecurity")
    private Boolean enhancedSecurity;

    public Boolean getEnhancedSecurity() {
        return enhancedSecurity;
    }

    public void setEnhancedSecurity(Boolean enhancedSecurity) {
        this.enhancedSecurity = enhancedSecurity;
    }
}
