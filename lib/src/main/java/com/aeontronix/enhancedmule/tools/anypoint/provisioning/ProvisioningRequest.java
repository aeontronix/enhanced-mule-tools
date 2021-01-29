/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.provisioning;

import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.ApplicationDescriptor;

public interface ProvisioningRequest {
    ApplicationDescriptor getApplicationDescriptor();

    String getBuildNumber();

    boolean isDeleteSnapshots();

    boolean isAutoApproveAPIAccessRequest();
}
