package com.aeontronix.enhancedmule.tools.fabric;

/**
 * Created by JacksonGenerator on 10/31/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class FabricNode {
    @JsonProperty("uid")
    private String uid;
    @JsonProperty("role")
    private String role;
    @JsonProperty("dockerVersion")
    private String dockerVersion;
    @JsonProperty("name")
    private String name;
    @JsonProperty("kubeletVersion")
    private String kubeletVersion;
    @JsonProperty("allocatedRequestCapacity")
    private FabricCapacity allocatedRequestCapacity;
    @JsonProperty("status")
    private FabricStatus status;
    @JsonProperty("capacity")
    private FabricCapacity capacity;
    @JsonProperty("allocatedLimitCapacity")
    private FabricCapacity allocatedLimitCapacity;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDockerVersion() {
        return dockerVersion;
    }

    public void setDockerVersion(String dockerVersion) {
        this.dockerVersion = dockerVersion;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKubeletVersion() {
        return kubeletVersion;
    }

    public void setKubeletVersion(String kubeletVersion) {
        this.kubeletVersion = kubeletVersion;
    }

    public FabricCapacity getAllocatedRequestCapacity() {
        return allocatedRequestCapacity;
    }

    public void setAllocatedRequestCapacity(FabricCapacity allocatedRequestCapacity) {
        this.allocatedRequestCapacity = allocatedRequestCapacity;
    }

    public FabricStatus getStatus() {
        return status;
    }

    public void setStatus(FabricStatus status) {
        this.status = status;
    }

    public FabricCapacity getCapacity() {
        return capacity;
    }

    public void setCapacity(FabricCapacity capacity) {
        this.capacity = capacity;
    }

    public FabricCapacity getAllocatedLimitCapacity() {
        return allocatedLimitCapacity;
    }

    public void setAllocatedLimitCapacity(FabricCapacity allocatedLimitCapacity) {
        this.allocatedLimitCapacity = allocatedLimitCapacity;
    }
}
