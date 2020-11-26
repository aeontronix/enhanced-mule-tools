/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.application.deploy;

public class RTFDeploymentConfig {
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
    private DeploymentModel updateStrategy;
    private int replicas;
    private boolean forwardSslSession;

    public RTFDeploymentConfig() {
    }

    public RTFDeploymentConfig(String cpuReserved, String cpuLimit, String memoryReserved, String memoryLimit, boolean clustered, boolean enforceDeployingReplicasAcrossNodes, String httpInboundPublicUrl, String jvmArgs, String runtimeVersion, boolean lastMileSecurity, boolean forwardSslSession, DeploymentModel updateStrategy, int replicas) {
        this.cpuReserved = cpuReserved;
        this.cpuLimit = cpuLimit;
        this.memoryReserved = memoryReserved;
        this.memoryLimit = memoryLimit;
        this.clustered = clustered;
        this.enforceDeployingReplicasAcrossNodes = enforceDeployingReplicasAcrossNodes;
        this.httpInboundPublicUrl = httpInboundPublicUrl;
        this.jvmArgs = jvmArgs;
        this.runtimeVersion = runtimeVersion;
        this.lastMileSecurity = lastMileSecurity;
        this.updateStrategy = updateStrategy;
        this.replicas = replicas;
        this.forwardSslSession = forwardSslSession;
    }

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

    public DeploymentModel getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(DeploymentModel updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    public boolean isForwardSslSession() {
        return forwardSslSession;
    }

    public void setForwardSslSession(boolean forwardSslSession) {
        this.forwardSslSession = forwardSslSession;
    }

    public int getReplicas() {
        return replicas;
    }

    public void setReplicas(int replicas) {
        this.replicas = replicas;
    }


    public enum DeploymentModel {
        ROLLING, RECREATE
    }
}
