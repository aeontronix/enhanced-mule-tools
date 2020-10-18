/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning.api;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.TempFile;
import com.aeontronix.commons.validation.ValidationUtils;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetCategory;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetCreationException;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetTag;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.api.API;
import com.aeontronix.enhancedmule.tools.api.APISpec;
import com.aeontronix.enhancedmule.tools.api.SLATier;
import com.aeontronix.enhancedmule.tools.api.SLATierLimits;
import com.aeontronix.enhancedmule.tools.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.provisioning.portal.PortalDescriptor;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class APIDescriptor {
    private static final Logger logger = LoggerFactory.getLogger(APIDescriptor.class);
    private String assetId;
    private String assetVersion;
    private String version;
    /**
     * Backwards compatibility, use 'version' instead
     */
    @Deprecated
    private String apiVersion;
    private String endpoint;
    private List<String> tags;
    private List<String> exchangeTags;
    private boolean addAutoDescovery = false;
    private String autoDiscoveryFlow = "api-main";
    private HashMap<String, Object> endpointJson;
    private List<PolicyDescriptor> policies;
    private List<String> accessedBy;
    private String label;
    private List<SLATierDescriptor> slaTiers;
    private API.Type type = API.Type.REST;
    private boolean assetCreate;
    private String assetMainFile;
    private PortalDescriptor portal;
    private Map<String,List<String>> categories;

    public APIDescriptor() {
    }

    public APIDescriptor(String assetId, String version) {
        this.assetId = assetId;
        this.assetVersion = version;
    }

    public void provision(ApplicationDescriptor cfg, Environment environment, APIProvisioningConfig config, ApplicationSource applicationSource, APIProvisioningResult result) throws ProvisioningException {
        try {
            if (version == null && apiVersion != null) {
                // backwards compatibility
                version = apiVersion;
            }
            ValidationUtils.notEmpty(IllegalStateException.class, "API Descriptor missing value: assetId", assetId);
            ValidationUtils.notEmpty(IllegalStateException.class, "API Descriptor missing value: assetVersion", assetVersion);
            logger.info("Provisioning " + this.getAssetId() + " within org " + environment.getParent().getName() + " env " + environment.getName());
            Boolean m3 = cfg.getMule3();
            if (m3 == null) {
                m3 = false;
            }
            API api = null;
            boolean updateEndpoint = true;
            try {
                api = environment.findAPIByExchangeAssetIdOrNameAndVersion(this.getAssetId(), this.getAssetVersion(), label);
                logger.debug("API " + this.getAssetId() + " " + this.getAssetVersion() + " exists: " + api);
            } catch (NotFoundException e) {
                logger.info("API " + this.getAssetId() + " " + this.getAssetVersion() + " not found, creating");
                APISpec apiSpec = null;
                try {
                    apiSpec = environment.getParent().findAPISpecsByIdOrNameAndVersion(this.getAssetId(), this.getAssetVersion());
                } catch (NotFoundException ex) {
                    if (assetCreate) {
                        logger.info("Asset not found, creating");
                        if (type == API.Type.HTTP) {
                            environment.getOrganization().createExchangeHTTPAPIAsset(null, assetId, assetId, assetVersion, "v1");
                        } else {
                            if (applicationSource == null) {
                                throw new AssetCreationException("Cannot create asset due to missing application source (standalone provisioning doesn't support REST asset creation)");
                            }
                            if (StringUtils.isBlank(assetMainFile)) {
                                throw new AssetCreationException("assetMainFile is required for API asset creation");
                            }
                            String assetClassifier = assetMainFile.toLowerCase().endsWith(".raml") ? "raml" : "oas";
                            try (TempFile apiSpecFile = new TempFile(assetId + "-" + assetVersion)) {
                                applicationSource.getAPISpecificationFiles(assetId, assetVersion, assetMainFile, assetClassifier, apiSpecFile);
                                environment.getOrganization().publishExchangeAPIAsset(assetId, assetVersion,
                                        version, assetClassifier, assetMainFile, apiSpecFile);
                            }
                        }
                    } else {
                        throw ex;
                    }
                    apiSpec = environment.getParent().findAPISpecsByIdOrNameAndVersion(this.getAssetId(), this.getAssetVersion());
                }
                // now we need to check if there's an existing API with the same productAPIVersion
                String productAPIVersion = apiSpec.getProductAPIVersion();
                try {
                    logger.debug("findAPIByExchangeAssetIdOrNameAndProductAPIVersion: {} , {} , {}", this.getAssetId(), productAPIVersion, label);
                    api = environment.findAPIByExchangeAssetIdOrNameAndProductAPIVersion(this.getAssetId(), productAPIVersion, label);
                    final String currentAssetVersion = api.getAssetVersion();
                    if (!currentAssetVersion.equalsIgnoreCase(assetVersion)) {
                        api = api.updateVersion(assetVersion);
                        logger.info("Changed asset version from {} to {}", currentAssetVersion, assetVersion);
                    }
                } catch (NotFoundException ex) {
                    logger.debug("Creating API");
                    if (endpointJson != null) {
                        api = environment.createAPI(apiSpec, label, endpointJson);
                    } else {
                        api = environment.createAPI(apiSpec, !m3, this.getEndpoint(), label, type);
                    }
                    updateEndpoint = false;
                }
            }
            if (policies != null) {
                api.deletePolicies();
                for (PolicyDescriptor policyDescriptor : policies) {
                    api.createPolicy(policyDescriptor);
                }
            }
            if (slaTiers != null) {
                for (SLATierDescriptor slaTierDescriptor : slaTiers) {
                    try {
                        SLATier slaTier = api.findSLATier(slaTierDescriptor.getName());
                        slaTier.setAutoApprove(slaTierDescriptor.isAutoApprove());
                        slaTier.setDescription(slaTierDescriptor.getDescription());
                        slaTier.setLimits(slaTierDescriptor.getLimits());
                        slaTier = slaTier.update();
                    } catch (NotFoundException e) {
                        api.createSLATier(slaTierDescriptor.getName(), slaTierDescriptor.getDescription(), slaTierDescriptor.isAutoApprove(), slaTierDescriptor.getLimits());
                    }
                }
            }
            api = updateEndpoint(m3, api, updateEndpoint);
            result.setApi(api);
            // exchange
            ExchangeAsset exchangeAsset = environment.getOrganization().findExchangeAsset(api.getGroupId(), api.getAssetId());
            exchangeAsset = updateExchangeTags(exchangeAsset);
            updateExchangeCategories(exchangeAsset);
            // portal
            if (portal != null) {
                portal.provision(exchangeAsset);
            }
        } catch (AssetCreationException | NotFoundException | IOException e) {
            throw new ProvisioningException(e);
        }
    }

    private void updateExchangeCategories(ExchangeAsset exchangeAsset) throws HttpException {
        final Map<String, List<String>> assetCategories = exchangeAsset.getCategories().stream().collect(
                toMap(AssetCategory::getKey, AssetCategory::getValue));
        if( categories == null ) {
            categories = new HashMap<>();
        }
        for (String curCatKey : assetCategories.keySet()) {
            if( ! categories.containsKey(curCatKey) ) {
                exchangeAsset.deleteCategory(curCatKey);
            }
        }
        for (Map.Entry<String, List<String>> catEntries : categories.entrySet()) {
            List<String> catValues = catEntries.getValue() != null ? catEntries.getValue() : Collections.emptyList();
            List<String> assetCatValues = assetCategories.getOrDefault(catEntries.getKey(),Collections.emptyList());
            if( !catValues.equals(assetCatValues)) {
                exchangeAsset.updateCategory(catEntries.getKey(),catValues);
            }
        }
    }

    private API updateEndpoint(Boolean m3, API api, boolean updateEndpoint) throws HttpException {
        if (updateEndpoint) {
            if (this.endpointJson != null) {
                api = api.updateEndpoint(this.getEndpoint(), endpointJson);
            } else if (this.getEndpoint() != null) {
                api = api.updateEndpoint(this.getEndpoint(), !m3, type);
            }
        }
        return api;
    }

    private ExchangeAsset updateExchangeTags(ExchangeAsset exchangeAsset) throws HttpException {
        ArrayList<String> current = exchangeAsset.getLabels().stream().map(AssetTag::getValue).collect(Collectors.toCollection(ArrayList::new));
        List<String> expectedTags = this.exchangeTags != null ? this.exchangeTags : Collections.emptyList();
        if (!current.equals(expectedTags)) {
            exchangeAsset = exchangeAsset.updateLabels(expectedTags);
            logger.info("Updated exchange tags to " + expectedTags);
        }
        return exchangeAsset;
    }

    @JsonProperty
    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @JsonProperty
    public String getAssetVersion() {
        return assetVersion;
    }

    public void setAssetVersion(String assetVersion) {
        this.assetVersion = assetVersion;
    }

    @JsonProperty
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public HashMap<String, Object> getEndpointJson() {
        return endpointJson;
    }

    public void setEndpointJson(HashMap<String, Object> endpointJson) {
        this.endpointJson = endpointJson;
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

    public API.Type getType() {
        return type;
    }

    public void setType(API.Type type) {
        this.type = type;
    }

    public boolean isAssetCreate() {
        return assetCreate;
    }

    public void setAssetCreate(boolean assetCreate) {
        this.assetCreate = assetCreate;
    }

    public boolean isAddAutoDescovery() {
        return addAutoDescovery;
    }

    public void setAddAutoDescovery(boolean addAutoDescovery) {
        this.addAutoDescovery = addAutoDescovery;
    }

    public String getAutoDiscoveryFlow() {
        return autoDiscoveryFlow;
    }

    public void setAutoDiscoveryFlow(String autoDiscoveryFlow) {
        this.autoDiscoveryFlow = autoDiscoveryFlow;
    }

    public String getAssetMainFile() {
        return assetMainFile;
    }

    public void setAssetMainFile(String assetMainFile) {
        this.assetMainFile = assetMainFile;
    }

    @Deprecated
    public String getApiVersion() {
        return apiVersion;
    }

    @Deprecated
    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getExchangeTags() {
        return exchangeTags;
    }

    public void setExchangeTags(List<String> exchangeTags) {
        this.exchangeTags = exchangeTags;
    }

    public PortalDescriptor getPortal() {
        return portal;
    }

    public void setPortal(PortalDescriptor portal) {
        this.portal = portal;
    }

    public Map<String, List<String>> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, List<String>> categories) {
        this.categories = categories;
    }
}
