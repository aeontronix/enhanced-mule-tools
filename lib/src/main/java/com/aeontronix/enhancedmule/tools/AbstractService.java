/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

public abstract class AbstractService implements Service {
    protected AnypointClient client;

    @Override
    public void setClient(AnypointClient client) {
        this.client = client;
    }
}
