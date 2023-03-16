/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application.deployment;

import com.aeontronix.anypointsdk.cloudhub2.CH2AppDeploymentParameters;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.RTFDeploymentConfig;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class DeploymentParameters {
    private String target;
    private CloudhubDeploymentParameters cloudhub;
    private CH2AppDeploymentParameters cloudhub2;
    private RTFDeploymentParameters rtf;
    private Duration deployTimeout;
    private Duration deployRetryDelay;
    private Boolean mergeExistingProperties;
    private Boolean mergeExistingPropertiesOverride;
    private Boolean extMonitoring;
    private Boolean autoApproveAccess;

    public DeploymentParameters() {
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    @NotNull
    public synchronized CloudhubDeploymentParameters getCloudhub() {
        if (cloudhub == null) {
            cloudhub = new CloudhubDeploymentParameters();
        }
        return cloudhub;
    }

    public void setCloudhub(CloudhubDeploymentParameters cloudhub) {
        this.cloudhub = cloudhub;
    }

    public synchronized CH2AppDeploymentParameters getCloudhub2() {
        return cloudhub2;
    }

    public void setCloudhub2(CH2AppDeploymentParameters cloudhub2) {
        this.cloudhub2 = cloudhub2;
    }

    @NotNull
    public RTFDeploymentParameters getRtf() {
        if (rtf == null) {
            rtf = new RTFDeploymentParameters();
        }
        return rtf;
    }

    public void setRtf(RTFDeploymentParameters rtf) {
        this.rtf = rtf;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    public Duration getDeployTimeout() {
        return deployTimeout;
    }

    public void setDeployTimeout(Duration deployTimeout) {
        this.deployTimeout = deployTimeout;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING)
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

    public void merge(DeploymentParameters ov) {
//        DescriptorHelper.overrideAll(this, ov, "cloudhub", "rtf");
//        DescriptorHelper.overrideAll(getCloudhub(), ov.getCloudhub());
//        DescriptorHelper.overrideAll(getRtf(), ov.getRtf());
    }

    public static DeploymentParameters createDefault() {
        final DeploymentParameters dp = new DeploymentParameters();
        dp.setDeployTimeout(Duration.ofMinutes(15));
        dp.setDeployRetryDelay(Duration.ofSeconds(3));
        dp.setMergeExistingProperties(false);
        dp.setMergeExistingPropertiesOverride(false);
        dp.setExtMonitoring(true);
        dp.setAutoApproveAccess(true);
        final CloudhubDeploymentParameters ch = dp.getCloudhub();
        ch.setAppNameSuffixNPOnly(false);
        ch.setObjectStoreV1(false);
        ch.setPersistentQueues(false);
        ch.setPersistentQueuesEncrypted(false);
        ch.setCustomlog4j(false);
        ch.setStaticIPs(false);
        ch.setWorkerCount(1);
        final RTFDeploymentParameters rtf = dp.getRtf();
        rtf.setCpuReserved("20m");
        rtf.setCpuLimit("1700m");
        rtf.setCpuLimit("1700m");
        rtf.setMemoryReserved("700Mi");
        rtf.setMemoryLimit("700Mi");
        rtf.setClustered(false);
        rtf.setEnforceDeployingReplicasAcrossNodes(false);
        rtf.setUpdateStrategy(RTFDeploymentConfig.DeploymentModel.ROLLING);
        rtf.setReplicas(1);
        return dp;
    }
}
