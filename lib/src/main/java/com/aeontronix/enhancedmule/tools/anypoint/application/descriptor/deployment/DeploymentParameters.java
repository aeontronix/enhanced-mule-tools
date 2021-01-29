/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class DeploymentParameters {
    private CloudhubDeploymentParameters cloudhub;
    private RTFDeploymentParameters rtf;
    private Duration deployTimeout = Duration.ofMinutes(15);
    private Duration deployRetryDelay = Duration.ofSeconds(3);
    private Boolean mergeExistingProperties;
    private Boolean mergeExistingPropertiesOverride;
    private Boolean extMonitoring;
    private Boolean autoApproveAccess;

    public DeploymentParameters() {
    }

    public DeploymentParameters(CloudhubDeploymentParameters cloudhub, RTFDeploymentParameters rtf, Duration deployTimeout,
                                Duration deployRetryDelay, Boolean mergeExistingProperties, Boolean mergeExistingPropertiesOverride,
                                Boolean extMonitoring) {
        this.cloudhub = cloudhub;
        this.rtf = rtf;
        this.deployTimeout = deployTimeout;
        this.deployRetryDelay = deployRetryDelay;
        this.mergeExistingProperties = mergeExistingProperties;
        this.mergeExistingPropertiesOverride = mergeExistingPropertiesOverride;
        this.extMonitoring = extMonitoring;
    }

    @NotNull
    public CloudhubDeploymentParameters getCloudhub() {
        if( cloudhub == null ) {
            cloudhub = new CloudhubDeploymentParameters();
        }
        return cloudhub;
    }

    public void setCloudhub(CloudhubDeploymentParameters cloudhub) {
        this.cloudhub = cloudhub;
    }

    @NotNull
    public RTFDeploymentParameters getRtf() {
        if( rtf == null ) {
            rtf = new RTFDeploymentParameters();
        }
        return rtf;
    }

    public void setRtf(RTFDeploymentParameters rtf) {
        this.rtf = rtf;
    }

    public Duration getDeployTimeout() {
        return deployTimeout;
    }

    public void setDeployTimeout(Duration deployTimeout) {
        this.deployTimeout = deployTimeout;
    }

    public Duration getDeployRetryDelay() {
        return deployRetryDelay;
    }

    public void setDeployRetryDelay(Duration deployRetryDelay) {
        this.deployRetryDelay = deployRetryDelay;
    }

    public Boolean getMergeExistingProperties() {
        return mergeExistingProperties;
    }

    public void setMergeExistingProperties(Boolean mergeExistingProperties) {
        this.mergeExistingProperties = mergeExistingProperties;
    }

    public Boolean getMergeExistingPropertiesOverride() {
        return mergeExistingPropertiesOverride;
    }

    public void setMergeExistingPropertiesOverride(Boolean mergeExistingPropertiesOverride) {
        this.mergeExistingPropertiesOverride = mergeExistingPropertiesOverride;
    }

    public Boolean getExtMonitoring() {
        return extMonitoring;
    }

    public void setExtMonitoring(Boolean extMonitoring) {
        this.extMonitoring = extMonitoring;
    }

    public Boolean getAutoApproveAccess() {
        return autoApproveAccess;
    }

    public void setAutoApproveAccess(Boolean autoApproveAccess) {
        this.autoApproveAccess = autoApproveAccess;
    }
}
