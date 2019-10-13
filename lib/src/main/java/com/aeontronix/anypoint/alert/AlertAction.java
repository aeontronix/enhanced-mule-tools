/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.anypoint.alert;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AlertAction {
    @JsonProperty
    private List<String> emails;
    @JsonProperty
    private List<String> userIds;
    @JsonProperty
    private String subject;
    @JsonProperty
    private String type;
    @JsonProperty
    private String content;

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
