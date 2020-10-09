/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.alert;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointObject;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class AlertUpdate extends AnypointObject {
    @JsonProperty(required = true)
    private AlertSeverity severity;
    @JsonProperty(required = true)
    private String name;
    @JsonProperty(required = true)
    private AlertCondition condition;
    private List<AlertAction> actions;

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AlertCondition getCondition() {
        return condition;
    }

    public void setCondition(AlertCondition condition) {
        this.condition = condition;
    }

    public List<AlertAction> getActions() {
        return actions;
    }

    public void setActions(List<AlertAction> actions) {
        this.actions = actions;
    }
}
