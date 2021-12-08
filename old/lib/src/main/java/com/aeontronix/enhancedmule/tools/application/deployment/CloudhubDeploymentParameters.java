/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.application.deployment;

public class CloudhubDeploymentParameters {
    private String appNameSuffix;
    private Boolean appNameSuffixNPOnly;
    private String appNamePrefix;
    private String muleVersion;
    private Boolean persistentQueues;
    private Boolean persistentQueuesEncrypted;
    private Boolean objectStoreV1;
    private Boolean customlog4j;
    private Boolean staticIPs;
    private String region;
    private String workerType;
    private Integer workerCount;

    public String getAppNameSuffix() {
        return appNameSuffix;
    }

    public void setAppNameSuffix(String appNameSuffix) {
        this.appNameSuffix = appNameSuffix;
    }

    public Boolean getAppNameSuffixNPOnly() {
        return appNameSuffixNPOnly;
    }

    public void setAppNameSuffixNPOnly(Boolean appNameSuffixNPOnly) {
        this.appNameSuffixNPOnly = appNameSuffixNPOnly;
    }

    public String getAppNamePrefix() {
        return appNamePrefix;
    }

    public void setAppNamePrefix(String appNamePrefix) {
        this.appNamePrefix = appNamePrefix;
    }

    public String getMuleVersion() {
        return muleVersion;
    }

    public void setMuleVersion(String muleVersion) {
        this.muleVersion = muleVersion;
    }

    public Boolean getPersistentQueues() {
        return persistentQueues;
    }

    public void setPersistentQueues(Boolean persistentQueues) {
        this.persistentQueues = persistentQueues;
    }

    public Boolean getPersistentQueuesEncrypted() {
        return persistentQueuesEncrypted;
    }

    public void setPersistentQueuesEncrypted(Boolean persistentQueuesEncrypted) {
        this.persistentQueuesEncrypted = persistentQueuesEncrypted;
    }

    public Boolean getObjectStoreV1() {
        return objectStoreV1;
    }

    public void setObjectStoreV1(Boolean objectStoreV1) {
        this.objectStoreV1 = objectStoreV1;
    }

    public Boolean getCustomlog4j() {
        return customlog4j;
    }

    public void setCustomlog4j(Boolean customlog4j) {
        this.customlog4j = customlog4j;
    }

    public Boolean getStaticIPs() {
        return staticIPs;
    }

    public void setStaticIPs(Boolean staticIPs) {
        this.staticIPs = staticIPs;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getWorkerType() {
        return workerType;
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
    }

    public Integer getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(Integer workerCount) {
        this.workerCount = workerCount;
    }
}
