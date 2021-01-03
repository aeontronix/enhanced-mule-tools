/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.provisioning;

public class ProvisioningRequest {
    private String id;
    private boolean deleteSnapshots = true;

    public ProvisioningRequest() {
    }

    public ProvisioningRequest(String id, boolean deleteSnapshots) {
        this.id = id;
        this.deleteSnapshots = deleteSnapshots;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDeleteSnapshots() {
        return deleteSnapshots;
    }

    public void setDeleteSnapshots(boolean deleteSnapshots) {
        this.deleteSnapshots = deleteSnapshots;
    }
}
