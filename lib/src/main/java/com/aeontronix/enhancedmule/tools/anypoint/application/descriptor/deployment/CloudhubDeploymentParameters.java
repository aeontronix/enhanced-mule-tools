/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.deployment;

public class CloudhubDeploymentParameters {
    private String appNameSuffix;
    private boolean appNameSuffixNPOnly;
    private String appNamePrefix;
    private String muleVersion;
    private boolean persistentQueues;
    private boolean persistentQueuesEncrypted;
    private boolean objectStoreV1 = false;
    private boolean customlog4j;
    private boolean staticIPs;
    private String region;
    private String workerType;
    private Integer workerCount;

    public String getAppNameSuffix() {
        return appNameSuffix;
    }

    public void setAppNameSuffix(String appNameSuffix) {
        this.appNameSuffix = appNameSuffix;
    }

    public boolean isAppNameSuffixNPOnly() {
        return appNameSuffixNPOnly;
    }

    public void setAppNameSuffixNPOnly(boolean appNameSuffixNPOnly) {
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

    public boolean isPersistentQueues() {
        return persistentQueues;
    }

    public void setPersistentQueues(boolean persistentQueues) {
        this.persistentQueues = persistentQueues;
    }

    public boolean isPersistentQueuesEncrypted() {
        return persistentQueuesEncrypted;
    }

    public void setPersistentQueuesEncrypted(boolean persistentQueuesEncrypted) {
        this.persistentQueuesEncrypted = persistentQueuesEncrypted;
    }

    public boolean isObjectStoreV1() {
        return objectStoreV1;
    }

    public void setObjectStoreV1(boolean objectStoreV1) {
        this.objectStoreV1 = objectStoreV1;
    }

    public boolean isStaticIPs() {
        return staticIPs;
    }

    public void setStaticIPs(boolean staticIPs) {
        this.staticIPs = staticIPs;
    }

    public boolean isCustomlog4j() {
        return customlog4j;
    }

    public void setCustomlog4j(boolean customlog4j) {
        this.customlog4j = customlog4j;
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
