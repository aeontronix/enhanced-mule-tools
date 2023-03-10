/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.anypoint.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SLATierLimits {
    @JsonProperty
    private boolean visible;
    @JsonProperty(required = true)
    private int timePeriodInMilliseconds;
    @JsonProperty(required = true)
    private int maximumRequests;

    public SLATierLimits() {
    }

    public SLATierLimits(boolean visible, int timePeriodInMilliseconds, int maximumRequests) {
        this.visible = visible;
        this.timePeriodInMilliseconds = timePeriodInMilliseconds;
        this.maximumRequests = maximumRequests;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public int getTimePeriodInMilliseconds() {
        return timePeriodInMilliseconds;
    }

    public void setTimePeriodInMilliseconds(int timePeriodInMilliseconds) {
        this.timePeriodInMilliseconds = timePeriodInMilliseconds;
    }

    public int getMaximumRequests() {
        return maximumRequests;
    }

    public void setMaximumRequests(int maximumRequests) {
        this.maximumRequests = maximumRequests;
    }
}
