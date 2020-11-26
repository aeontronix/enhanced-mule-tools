package com.aeontronix.enhancedmule.tools.fabric;

/**
 * Created by JacksonGenerator on 10/31/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class FabricStatus {
    @JsonProperty("isReady")
    private Boolean isReady;
    @JsonProperty("isHealthy")
    private Boolean isHealthy;
    @JsonProperty("isSchedulable")
    private Boolean isSchedulable;

    public Boolean getReady() {
        return isReady;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }

    public Boolean getHealthy() {
        return isHealthy;
    }

    public void setHealthy(Boolean healthy) {
        isHealthy = healthy;
    }

    public Boolean getSchedulable() {
        return isSchedulable;
    }

    public void setSchedulable(Boolean schedulable) {
        isSchedulable = schedulable;
    }
}
