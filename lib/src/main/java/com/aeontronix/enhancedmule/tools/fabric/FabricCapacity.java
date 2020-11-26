package com.aeontronix.enhancedmule.tools.fabric;

/**
 * Created by JacksonGenerator on 10/31/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;


public class FabricCapacity {
    @JsonProperty("memory")
    private String memory;
    @JsonProperty("cpu")
    private Integer cpu;
    @JsonProperty("memoryMi")
    private Integer memoryMi;
    @JsonProperty("pods")
    private Integer pods;
    @JsonProperty("cpuMillis")
    private Integer cpuMillis;

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public Integer getCpu() {
        return cpu;
    }

    public void setCpu(Integer cpu) {
        this.cpu = cpu;
    }

    public Integer getMemoryMi() {
        return memoryMi;
    }

    public void setMemoryMi(Integer memoryMi) {
        this.memoryMi = memoryMi;
    }

    public Integer getPods() {
        return pods;
    }

    public void setPods(Integer pods) {
        this.pods = pods;
    }

    public Integer getCpuMillis() {
        return cpuMillis;
    }

    public void setCpuMillis(Integer cpuMillis) {
        this.cpuMillis = cpuMillis;
    }
}
