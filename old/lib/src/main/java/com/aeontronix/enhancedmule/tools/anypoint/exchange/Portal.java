/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.exchange;

/**
 * Created by JacksonGenerator on 10/10/20.
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class Portal {
    @JsonProperty("organizationId")
    private String organizationId;
    @JsonProperty("versionId")
    private String versionId;
    @JsonProperty("createdDate")
    private String createdDate;
    @JsonProperty("pages")
    private List<PagesItem> pages;
    @JsonProperty("draftStatus")
    private String draftStatus;
    @JsonProperty("assetId")
    private String assetId;
    @JsonProperty("versionGroup")
    private String versionGroup;
    @JsonProperty("groupId")
    private String groupId;
    @JsonProperty("id")
    private String id;
    @JsonProperty("updatedDate")
    private String updatedDate;
    @JsonProperty("publishedVersionReferenceId")
    private String publishedVersionReferenceId;
    @JsonProperty("projectId")
    private String projectId;

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public List<PagesItem> getPages() {
        return pages;
    }

    public void setPages(List<PagesItem> pages) {
        this.pages = pages;
    }

    public String getDraftStatus() {
        return draftStatus;
    }

    public void setDraftStatus(String draftStatus) {
        this.draftStatus = draftStatus;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(String versionGroup) {
        this.versionGroup = versionGroup;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getPublishedVersionReferenceId() {
        return publishedVersionReferenceId;
    }

    public void setPublishedVersionReferenceId(String publishedVersionReferenceId) {
        this.publishedVersionReferenceId = publishedVersionReferenceId;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }
}
