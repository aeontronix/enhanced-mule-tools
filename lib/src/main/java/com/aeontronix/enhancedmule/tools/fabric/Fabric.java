package com.aeontronix.enhancedmule.tools.fabric;

/**
 * Created by JacksonGenerator on 10/31/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class Fabric {
    @JsonProperty("appliance")
    private FabricAppliance appliance;
    @JsonProperty("lastUpgradeTimestamp")
    private Long lastUpgradeTimestamp;
    @JsonProperty("version")
    private String version;
    @JsonProperty("clusterVersion")
    private String clusterVersion;
    @JsonProperty("availableUpgradeVersion")
    private String availableUpgradeVersion;
    @JsonProperty("secondsSinceHeartbeat")
    private Integer secondsSinceHeartbeat;
    @JsonProperty("isManaged")
    private Boolean isManaged;
    @JsonProperty("organizationId")
    private String organizationId;
    @JsonProperty("createdAt")
    private Long createdAt;
    @JsonProperty("features")
    private FabricFeatures features;
    @JsonProperty("nodes")
    private List<FabricNode> nodes;
    @JsonProperty("clusterConfigurationLevel")
    private String clusterConfigurationLevel;
    @JsonProperty("vendor")
    private String vendor;
    @JsonProperty("name")
    private String name;
    @JsonProperty("id")
    private String id;
    @JsonProperty("region")
    private String region;
    @JsonProperty("status")
    private String status;
    @JsonProperty("kubernetesVersion")
    private String kubernetesVersion;

    public FabricAppliance getAppliance() {
        return appliance;
    }

    public void setAppliance(FabricAppliance appliance) {
        this.appliance = appliance;
    }

    public Long getLastUpgradeTimestamp() {
        return lastUpgradeTimestamp;
    }

    public void setLastUpgradeTimestamp(Long lastUpgradeTimestamp) {
        this.lastUpgradeTimestamp = lastUpgradeTimestamp;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getClusterVersion() {
        return clusterVersion;
    }

    public void setClusterVersion(String clusterVersion) {
        this.clusterVersion = clusterVersion;
    }

    public String getAvailableUpgradeVersion() {
        return availableUpgradeVersion;
    }

    public void setAvailableUpgradeVersion(String availableUpgradeVersion) {
        this.availableUpgradeVersion = availableUpgradeVersion;
    }

    public Integer getSecondsSinceHeartbeat() {
        return secondsSinceHeartbeat;
    }

    public void setSecondsSinceHeartbeat(Integer secondsSinceHeartbeat) {
        this.secondsSinceHeartbeat = secondsSinceHeartbeat;
    }

    public Boolean getManaged() {
        return isManaged;
    }

    public void setManaged(Boolean managed) {
        isManaged = managed;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public FabricFeatures getFeatures() {
        return features;
    }

    public void setFeatures(FabricFeatures features) {
        this.features = features;
    }

    public List<FabricNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<FabricNode> nodes) {
        this.nodes = nodes;
    }

    public String getClusterConfigurationLevel() {
        return clusterConfigurationLevel;
    }

    public void setClusterConfigurationLevel(String clusterConfigurationLevel) {
        this.clusterConfigurationLevel = clusterConfigurationLevel;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKubernetesVersion() {
        return kubernetesVersion;
    }

    public void setKubernetesVersion(String kubernetesVersion) {
        this.kubernetesVersion = kubernetesVersion;
    }
}
