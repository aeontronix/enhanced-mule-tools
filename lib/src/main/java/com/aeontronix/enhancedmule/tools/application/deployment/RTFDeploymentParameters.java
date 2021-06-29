/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application.deployment;

import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.RTFDeploymentConfig;

public class RTFDeploymentParameters {
    private String cpuReserved;
    private String cpuLimit;
    private String memoryReserved;
    private String memoryLimit;
    private Boolean clustered;
    private Boolean enforceDeployingReplicasAcrossNodes;
    private String httpInboundPublicUrl;
    private String jvmArgs;
    private String runtimeVersion;
    private Boolean lastMileSecurity;
    private Boolean forwardSslSession;
    private RTFDeploymentConfig.DeploymentModel updateStrategy;
    private Integer replicas;

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

    public Boolean getClustered() {
        return clustered;
    }

    public void setClustered(Boolean clustered) {
        this.clustered = clustered;
    }

    public Boolean getEnforceDeployingReplicasAcrossNodes() {
        return enforceDeployingReplicasAcrossNodes;
    }

    public void setEnforceDeployingReplicasAcrossNodes(Boolean enforceDeployingReplicasAcrossNodes) {
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

    public Boolean getLastMileSecurity() {
        return lastMileSecurity;
    }

    public void setLastMileSecurity(Boolean lastMileSecurity) {
        this.lastMileSecurity = lastMileSecurity;
    }

    public Boolean getForwardSslSession() {
        return forwardSslSession;
    }

    public void setForwardSslSession(Boolean forwardSslSession) {
        this.forwardSslSession = forwardSslSession;
    }

    public RTFDeploymentConfig.DeploymentModel getUpdateStrategy() {
        return updateStrategy;
    }

    public void setUpdateStrategy(RTFDeploymentConfig.DeploymentModel updateStrategy) {
        this.updateStrategy = updateStrategy;
    }

    public Integer getReplicas() {
        return replicas;
    }

    public void setReplicas(Integer replicas) {
        this.replicas = replicas;
    }
}
