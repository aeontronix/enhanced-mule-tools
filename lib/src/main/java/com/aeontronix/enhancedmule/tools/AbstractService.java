/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Service;

public abstract class AbstractService implements Service {
    protected LegacyAnypointClient client;

    @Override
    public void setClient(LegacyAnypointClient client) {
        this.client = client;
    }
}
