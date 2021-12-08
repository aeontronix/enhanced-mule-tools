/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.provisioning;

import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;

public class ProvisioningRequestImpl implements ProvisioningRequest {
    private String buildNumber;
    private boolean deleteSnapshots;
    private boolean autoApproveAPIAccessRequest;
    private ApplicationDescriptor applicationDescriptor;

    public ProvisioningRequestImpl() {
    }

    public ProvisioningRequestImpl(String buildNumber, boolean deleteSnapshots, boolean autoApproveAPIAccessRequest) {
        this.buildNumber = buildNumber;
        this.deleteSnapshots = deleteSnapshots;
        this.autoApproveAPIAccessRequest = autoApproveAPIAccessRequest;
    }

    @Override
    public String getBuildNumber() {
        return buildNumber;
    }

    public void setBuildNumber(String buildNumber) {
        this.buildNumber = buildNumber;
    }

    @Override
    public boolean isDeleteSnapshots() {
        return deleteSnapshots;
    }

    public void setDeleteSnapshots(boolean deleteSnapshots) {
        this.deleteSnapshots = deleteSnapshots;
    }

    @Override
    public ApplicationDescriptor getApplicationDescriptor() {
        return applicationDescriptor;
    }

    public void setApplicationDescriptor(ApplicationDescriptor applicationDescriptor) {
        this.applicationDescriptor = applicationDescriptor;
    }

    @Override
    public boolean isAutoApproveAPIAccessRequest() {
        return autoApproveAPIAccessRequest;
    }
}
