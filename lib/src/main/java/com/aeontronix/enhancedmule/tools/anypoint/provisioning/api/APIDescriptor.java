/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.provisioning.api;

import com.aeontronix.enhancedmule.tools.anypoint.api.SLATierLimits;
import com.aeontronix.enhancedmule.tools.exchange.ExchangeAssetDescriptor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class APIDescriptor {
    private static final Logger logger = LoggerFactory.getLogger(APIDescriptor.class);
    private ExchangeAssetDescriptor asset;
    private String implementationUrl;
    private String consumerUrl;
    private Map<String, Object> implementationUrlJson;
    private List<String> tags;
    private boolean addAutoDiscovery = false;
    private String autoDiscoveryFlow = "api-main";
    private List<PolicyDescriptor> policies;
    private List<String> accessedBy;
    private String label;
    private List<SLATierDescriptor> slaTiers;
    private String apiIdProperty;
    private boolean injectApiId = true;

    public APIDescriptor() {
    }

    @JsonProperty(defaultValue = "true")
    public boolean isInjectApiId() {
        return injectApiId;
    }

    public void setInjectApiId(boolean injectApiId) {
        this.injectApiId = injectApiId;
    }

    public void addPolicy(PolicyDescriptor policy) {
        getPolicies().add(policy);
    }

    @JsonProperty
    public synchronized List<PolicyDescriptor> getPolicies() {
        if (policies == null) {
            policies = new ArrayList<>();
        }
        return policies;
    }

    public void setPolicies(List<PolicyDescriptor> policies) {
        this.policies = policies;
    }

    @JsonProperty
    public List<String> getAccessedBy() {
        return accessedBy;
    }

    public void setAccessedBy(List<String> accessedBy) {
        this.accessedBy = accessedBy;
    }

    public synchronized List<SLATierDescriptor> getSlaTiers() {
        return slaTiers;
    }

    public synchronized void setSlaTiers(List<SLATierDescriptor> slaTiers) {
        this.slaTiers = slaTiers;
    }

    public synchronized APIDescriptor addSlaTier(String name, String description, boolean autoApprove, SLATierLimits... limits) {
        return addSlaTier(new SLATierDescriptor(name, description, autoApprove, limits));
    }

    public synchronized APIDescriptor addSlaTier(String name, boolean autoApprove, SLATierLimits... limits) {
        return addSlaTier(name, null, autoApprove, limits);
    }

    public synchronized APIDescriptor addSlaTier(SLATierDescriptor slaTierDescriptor) {
        if (slaTiers == null) {
            slaTiers = new ArrayList<>();
        }
        slaTiers.add(slaTierDescriptor);
        return this;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public boolean isAddAutoDiscovery() {
        return addAutoDiscovery;
    }

    public void setAddAutoDiscovery(boolean addAutoDiscovery) {
        this.addAutoDiscovery = addAutoDiscovery;
    }

    public String getAutoDiscoveryFlow() {
        return autoDiscoveryFlow;
    }

    public void setAutoDiscoveryFlow(String autoDiscoveryFlow) {
        this.autoDiscoveryFlow = autoDiscoveryFlow;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    @JsonProperty
    public String getImplementationUrl() {
        return implementationUrl;
    }

    public void setImplementationUrl(String implementationUrl) {
        this.implementationUrl = implementationUrl;
    }

    @JsonProperty
    public String getConsumerUrl() {
        return consumerUrl;
    }

    public void setConsumerUrl(String consumerUrl) {
        this.consumerUrl = consumerUrl;
    }

    @JsonProperty
    public Map<String, Object> getImplementationUrlJson() {
        return implementationUrlJson;
    }

    public void setImplementationUrlJson(Map<String, Object> implementationUrlJson) {
        this.implementationUrlJson = implementationUrlJson;
    }

    public String getApiIdProperty() {
        return apiIdProperty;
    }

    public void setApiIdProperty(String apiIdProperty) {
        this.apiIdProperty = apiIdProperty;
    }

    @JsonIgnore
    public String getAssetVersion() {
        return asset != null ? asset.getVersion() : null;
    }

    @JsonIgnore
    public synchronized void setAssetVersion(String version) {
        if (asset == null) {
            asset = new ExchangeAssetDescriptor();
        }
        asset.setVersion(version);
    }

    public ExchangeAssetDescriptor getAsset() {
        return asset;
    }

    public void setAsset(ExchangeAssetDescriptor asset) {
        this.asset = asset;
    }
}
