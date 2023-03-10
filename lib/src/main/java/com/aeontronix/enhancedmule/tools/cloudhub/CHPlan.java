/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.cloudhub;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CHPlan {
    private List<CHWorkerType> workerTypes;

    @JsonProperty
    public List<CHWorkerType> getWorkerTypes() {
        return workerTypes;
    }

    public void setWorkerTypes(List<CHWorkerType> workerTypes) {
        this.workerTypes = workerTypes;
    }
}
