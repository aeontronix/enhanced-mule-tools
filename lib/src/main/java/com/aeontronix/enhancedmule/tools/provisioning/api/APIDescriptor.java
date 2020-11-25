/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning.api;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.TempFile;
import com.aeontronix.commons.validation.ValidationUtils;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetCategory;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetCreationException;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetTag;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.api.API;
import com.aeontronix.enhancedmule.tools.api.APISpec;
import com.aeontronix.enhancedmule.tools.api.SLATier;
import com.aeontronix.enhancedmule.tools.api.SLATierLimits;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.provisioning.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.provisioning.portal.PortalDescriptor;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;

public class APIDescriptor {
    private static final Logger logger = LoggerFactory.getLogger(APIDescriptor.class);
    private static final EMTLogger plogger = new EMTLogger(logger);
    private String assetId;
    private String assetVersion;
    private String name;
    private String description;
    private String version;
    /**
     * Backwards compatibility, use 'version' instead
     */
    @Deprecated
    private String apiVersion;
    private String implementationUrl;
    private String consumerUrl;
    private Map<String,Object> implementationUrlJson;
    private List<String> tags;
    private List<String> exchangeTags;
    private boolean addAutoDiscovery = false;
    private String autoDiscoveryFlow = "api-main";
    private List<PolicyDescriptor> policies;
    private List<String> accessedBy;
    private String label;
    private List<SLATierDescriptor> slaTiers;
    private API.Type type = API.Type.REST;
    private Boolean assetCreate;
    private String assetMainFile;
    private PortalDescriptor portal;
    private Map<String, List<String>> categories;
    private List<APICustomFieldDescriptor> fields;
    private IconDescriptor icon;
    private String apiIdProperty;
    private boolean injectApiId = true;

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
            final Organization organization = environment.getParent();
            logger.info("Provisioning " + this.getAssetId() + " within org " + organization.getName() + " env " + environment.getName());
            Boolean m3 = cfg.getMule3();
            if (m3 == null) {
                m3 = false;
            }
            API api;
            boolean updateEndpoint = true;
            try {
                api = environment.findAPIByExchangeAssetIdOrNameAndVersion(this.getAssetId(), this.getAssetVersion(), label);
                logger.debug("API " + this.getAssetId() + " " + this.getAssetVersion() + " exists: " + api);
            } catch (NotFoundException e) {
                logger.debug("API " + this.getAssetId() + " " + this.getAssetVersion() + " not found, creating");
                APISpec apiSpec;
                try {
                    apiSpec = organization.findAPISpecsByIdOrNameAndVersion(this.getAssetId(), this.getAssetVersion());
                } catch (NotFoundException ex) {
                    if (assetCreate) {
                        if (type == API.Type.HTTP) {
                            environment.getOrganization().createExchangeHTTPAPIAsset(null, assetId, assetId, assetVersion, version);
                            plogger.info(EMTLogger.Product.EXCHANGE, "Created HTTP asset : {} : {} : {}", assetId, assetVersion, version);
                        } else {
                            if (applicationSource == null) {
                                throw new AssetCreationException("Cannot create asset due to missing application source (standalone provisioning doesn't support REST asset creation)");
                            }
                            if (StringUtils.isBlank(assetMainFile)) {
                                throw new AssetCreationException("assetMainFile is required for API asset creation");
                            }
                            String assetClassifier = assetMainFile.toLowerCase().endsWith(".raml") ? "raml" : "oas";
                            try (TempFile apiSpecFile = new TempFile(assetId + "-" + assetVersion)) {
                                applicationSource.copyAPISpecs(assetMainFile, apiSpecFile);
                                environment.getOrganization().publishExchangeAPIAsset(name, assetId,
                                        assetVersion, version, assetClassifier, assetMainFile, apiSpecFile);
                                plogger.info(EMTLogger.Product.EXCHANGE, "Created API asset : {} : {} : {}", assetId, assetVersion, version);
                            }
                        }
                    } else {
                        throw ex;
                    }
                    apiSpec = organization.findAPISpecsByIdOrNameAndVersion(this.getAssetId(), this.getAssetVersion());
                }
                // now we need to check if there's an existing API with the same productAPIVersion
                String productAPIVersion = apiSpec.getProductAPIVersion();
                try {
                    logger.debug("findAPIByExchangeAssetIdOrNameAndProductAPIVersion: {} , {} , {}", this.getAssetId(), productAPIVersion, label);
                    api = environment.findAPIByExchangeAssetIdOrNameAndProductAPIVersion(this.getAssetId(), productAPIVersion, label);
                    final String currentAssetVersion = api.getAssetVersion();
                    if (!currentAssetVersion.equalsIgnoreCase(assetVersion)) {
                        api = api.updateVersion(assetVersion);
                        plogger.info(EMTLogger.Product.API_MANAGER, "Updated asset {} version to {}",api.getAssetId(),assetVersion);
                    }
                } catch (NotFoundException ex) {
                    logger.debug("Creating API");
                    if (implementationUrlJson != null) {
                        api = environment.createAPI(apiSpec, label, implementationUrlJson, consumerUrl );
                    } else {
                        api = environment.createAPI(apiSpec, !m3, implementationUrl, consumerUrl , label, type);
                    }
                    plogger.info(EMTLogger.Product.API_MANAGER, "Created api {}",api.getAssetId(),assetVersion);
                    updateEndpoint = false;
                }
            }
            if (policies != null) {
                plogger.info(EMTLogger.Product.API_MANAGER, "Setting policies for {}",api.getAssetId());
                api.deletePolicies();
                for (PolicyDescriptor policyDescriptor : policies) {
                    api.createPolicy(policyDescriptor);
                }
            }
            if (slaTiers != null) {
                plogger.info(EMTLogger.Product.API_MANAGER, "Setting SLA Tiers for {}",api.getAssetId());
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
            if( consumerUrl != null ) {
                api.updateConsumerUrl(consumerUrl);
                plogger.info(EMTLogger.Product.API_MANAGER, "Updated consumer url to {}",consumerUrl);
            }
            if( implementationUrlJson !=null ) {
                api.updateImplementationUrl(implementationUrlJson);
                plogger.info(EMTLogger.Product.API_MANAGER, "Updated implementation url to {}", implementationUrlJson.toString());
            } else if( implementationUrl != null ) {
                api.updateImplementationUrl(implementationUrl, !m3, type );
                plogger.info(EMTLogger.Product.API_MANAGER, "Updated implementation url to {}",implementationUrl);
            }
            api = api.refresh();
            result.setApi(api);
            if (logger.isDebugEnabled()) {
                logger.debug("api: {}", api.toString());
            }
            // exchange
            ExchangeAsset exchangeAsset = environment.getOrganization().findExchangeAsset(api.getGroupId(), api.getAssetId());
            if (name != null && !name.equals(exchangeAsset.getName())) {
                exchangeAsset.updateName(name);
                plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' name",exchangeAsset.getAssetId());
            }
            if (description != null && !description.equals(exchangeAsset.getDescription())) {
                exchangeAsset.updateDescription(description);
                plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' description",exchangeAsset.getAssetId());
            }
            exchangeAsset = updateExchangeTags(exchangeAsset);
            if( icon != null ) {
                exchangeAsset.updateIcon(StringUtils.base64Decode(icon.getContent()),icon.getMimeType());
                plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' icon",exchangeAsset.getAssetId());
            }
            final ExchangeAsset.CustomFieldUpdateResults results = exchangeAsset.updateCustomFields(fields);
            for (String field : results.getModified()) {
                plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' custom field '{}'",exchangeAsset.getAssetId(),field);
            }
            for (String field : results.getNotDefined()) {
                logger.warn("Custom field not defined, assignment failed: " + field);
            }
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
        if (categories != null) {
            final Map<String, List<String>> assetCategories = exchangeAsset.getCategories().stream().collect(
                    toMap(AssetCategory::getKey, AssetCategory::getValue));
            for (String curCatKey : assetCategories.keySet()) {
                if (!categories.containsKey(curCatKey)) {
                    exchangeAsset.deleteCategory(curCatKey);
                    plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' category '{}'",exchangeAsset.getAssetId(),curCatKey);
                }
            }
            for (Map.Entry<String, List<String>> catEntries : categories.entrySet()) {
                List<String> catValues = catEntries.getValue() != null ? catEntries.getValue() : Collections.emptyList();
                final String catKey = catEntries.getKey();
                List<String> assetCatValues = assetCategories.getOrDefault(catKey, Collections.emptyList());
                if (!catValues.equals(assetCatValues)) {
                    exchangeAsset.updateCategory(catKey, catValues);
                    plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' category '{}' to '{}'",exchangeAsset.getAssetId(),catKey,catValues);
                }
            }
        }
    }

    private ExchangeAsset updateExchangeTags(ExchangeAsset exchangeAsset) throws HttpException {
        if (this.exchangeTags != null) {
            ArrayList<String> current = exchangeAsset.getLabels().stream().map(AssetTag::getValue).collect(Collectors.toCollection(ArrayList::new));
            List<String> expectedTags = this.exchangeTags;
            if (!current.equals(expectedTags)) {
                exchangeAsset = exchangeAsset.updateLabels(expectedTags);
                plogger.info(EMTLogger.Product.EXCHANGE, "Updated tags of {} : {}",exchangeAsset.getAssetId(),expectedTags);
            }
        }
        return exchangeAsset;
    }

    @JsonProperty(defaultValue = "true")
    public boolean isInjectApiId() {
        return injectApiId;
    }

    public void setInjectApiId(boolean injectApiId) {
        this.injectApiId = injectApiId;
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

    public Boolean isAssetCreate() {
        return assetCreate;
    }

    public void setAssetCreate(Boolean assetCreate) {
        this.assetCreate = assetCreate;
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

    public List<APICustomFieldDescriptor> getFields() {
        return fields;
    }

    public void setFields(List<APICustomFieldDescriptor> fields) {
        this.fields = fields;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public IconDescriptor getIcon() {
        return icon;
    }

    public void setIcon(IconDescriptor icon) {
        this.icon = icon;
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
}
