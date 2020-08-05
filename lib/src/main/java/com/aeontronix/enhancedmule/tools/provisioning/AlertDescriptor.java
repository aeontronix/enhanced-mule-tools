/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning;

import com.aeontronix.enhancedmule.tools.Environment;
import com.aeontronix.enhancedmule.tools.Organization;
import com.aeontronix.enhancedmule.tools.alert.AlertAction;
import com.aeontronix.enhancedmule.tools.alert.AlertSeverity;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.aeontronix.enhancedmule.tools.util.MarkdownHelper.writeHeader;
import static com.aeontronix.enhancedmule.tools.util.MarkdownHelper.writeParagraph;
import static java.util.stream.Collectors.joining;

public class AlertDescriptor {
    private String id;
    @JsonProperty(required = true)
    private AlertSeverity severity;
    @JsonProperty(required = true)
    private String name;
    @JsonProperty(required = true)
    private String conditionType;
    @JsonProperty(required = true)
    private List<String> conditionResources;
    @JsonProperty(required = true)
    private String conditionResourceType;
    @JsonProperty
    private List<AlertAction> actions;
    private List<ProvisioningScope> scopes = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public List<String> getConditionResources() {
        return conditionResources;
    }

    public void setConditionResources(List<String> conditionResources) {
        this.conditionResources = conditionResources;
    }

    public String getConditionResourceType() {
        return conditionResourceType;
    }

    public void setConditionResourceType(String conditionResourceType) {
        this.conditionResourceType = conditionResourceType;
    }

    public List<AlertAction> getActions() {
        return actions;
    }

    public void setActions(List<AlertAction> actions) {
        this.actions = actions;
    }

    @NotNull
    public List<ProvisioningScope> getScopes() {
        return scopes;
    }

    public void setScopes(@NotNull List<ProvisioningScope> scopes) {
        this.scopes = scopes;
    }

    public void toMarkdown(Writer w, int headingDepth) throws IOException {
        writeHeader(w, 2 + headingDepth, name);
        writeParagraph(w, "Severity: " + severity.name());
        writeParagraph(w, "Condition Type: " + conditionType);
        writeParagraph(w, "Condition Resource Type: " + conditionResourceType);
        writeParagraph(w, "Condition Resources: " + String.join(", ", conditionResources));
        writeParagraph(w, "Environment: " + scopes.stream().map(ProvisioningScope::toShortMarkdown).collect(joining(", ")));
        if (actions != null && !actions.isEmpty()) {
            writeParagraph(w, "Actions: ");
            for (AlertAction action : actions) {
                writeParagraph(w, "Email to: " + String.join(", ", action.getEmails().toArray(new String[0])));
                writeParagraph(w,"Email subject: "+action.getSubject());
                writeParagraph(w,"Content:");
                writeParagraph(w,"> "+action.getContent().replaceAll("\n","\n> "));
            }
        }
    }

    public void provision(Organization org, ArrayList<Environment> envs) {

    }
}
