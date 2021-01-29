/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment;

import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.RTFDeploymentConfig;

public class RTFDeploymentParameters {
    private String cpuReserved;
    private String cpuLimit;
    private String memoryReserved;
    private String memoryLimit;
    private boolean clustered;
    private boolean enforceDeployingReplicasAcrossNodes;
    private String httpInboundPublicUrl;
    private String jvmArgs;
    private String runtimeVersion;
    private boolean lastMileSecurity;
    private boolean forwardSslSession;
    private RTFDeploymentConfig.DeploymentModel updateStrategy;
    private int replicas;

    public String getCpuReserved() {
        return cpuReserved;
    }

    public void setCpuReserved(String cpuReserved) {
        this.cpuReserved = cpuReserved;
    }

    public String getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(String cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public String getMemoryReserved() {
        return memoryReserved;
    }

    public void setMemoryReserved(String memoryReserved) {
        this.memoryReserved = memoryReserved;
    }

    public String getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(String memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public boolean isClustered() {
        return clustered;
    }

    public void setClustered(boolean clustered) {
        this.clustered = clustered;
    }

    public boolean isEnforceDeployingReplicasAcrossNodes() {
        return enforceDeployingReplicasAcrossNodes;
    }

    public void setEnforceDeployingReplicasAcrossNodes(boolean enforceDeployingReplicasAcrossNodes) {
        this.enforceDeployingReplicasAcrossNodes = enforceDeployingReplicasAcrossNodes;
    }

    public String getHttpInboundPublicUrl() {
        return httpInboundPublicUrl;
    }

    public void setHttpInboundPublicUrl(String httpInboundPublicUrl) {
        this.httpInboundPublicUrl = httpInboundPublicUrl;
    }

    public String getJvmArgs() {
        return jvmArgs;
    }

    public void setJvmArgs(String jvmArgs) {
        this.jvmArgs = jvmArgs;
    }

    public String getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setRuntimeVersion(String runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }

    public boolean isLastMileSecurity() {
        return lastMileSecurity;
    }

    public void setLastMileSecurity(boolean lastMileSecurity) {
        this.lastMileSecurity = lastMileSecurity;
    }

    public boolean isForwardSslSession() {
        return forwardSslSession;
    }

    public void setForwardSslSession(boolean forwardSslSession) {
        this.forwardSslSession = forwardSslSession;
    }

    public RTFDeploymentConfig.DeploymentModel getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(RTFDeploymentConfig.DeploymentModel updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }
}
