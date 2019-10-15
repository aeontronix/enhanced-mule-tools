/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.anypoint.api;

import com.aeontronix.anypoint.AnypointClient;
import com.aeontronix.anypoint.AnypointObject;
import com.aeontronix.anypoint.HttpException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SLATier extends AnypointObject<API> {
    private Long id;
    private String name;
    private String status;
    private String description;
    private boolean autoApprove;
    private int applicationCount;
    private List<SLATierLimits> limits;

    public SLATier(AnypointClient client) {
        super(client);
    }

    public SLATier(API parent) {
        super(parent);
    }

    public SLATier() {
    }

    public static List<String> getNames(List<SLATier> slaTier) {
        ArrayList<String> names = new ArrayList<>(slaTier.size());
        for (SLATier tier : slaTier) {
            if (tier.getName() != null) {
                names.add(tier.getName());
            }
        }
        return names;
    }

    @JsonProperty
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void delete() throws HttpException {
        client.getHttpHelper().httpDelete(getUrl());
    }

    @NotNull
    private String getUrl() {
        return "/apimanager/api/v1/organizations/" +
                parent.getParent().getParent().getId() + "/environments/" + parent.getParent().getId() + "/apis/" + parent.getId() + "/tiers/" + id;
    }

    @JsonProperty
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty
    public boolean isAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(boolean autoApprove) {
        this.autoApprove = autoApprove;
    }

    @JsonProperty
    public int getApplicationCount() {
        return applicationCount;
    }

    public void setApplicationCount(int applicationCount) {
        this.applicationCount = applicationCount;
    }

    @JsonProperty
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty
    public List<SLATierLimits> getLimits() {
        return limits;
    }

    public void setLimits(List<SLATierLimits> limits) {
        this.limits = limits;
    }

    public SLATier update() throws HttpException {
        String json = client.getHttpHelper().httpPut(getUrl(), this);
        return client.getJsonHelper().readJson(new SLATier(parent), json);
    }
}
