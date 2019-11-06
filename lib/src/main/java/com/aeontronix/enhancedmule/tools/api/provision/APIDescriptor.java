/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.api.provision;

import com.aeontronix.enhancedmule.tools.Environment;
import com.aeontronix.enhancedmule.tools.HttpException;
import com.aeontronix.enhancedmule.tools.NotFoundException;
import com.aeontronix.enhancedmule.tools.Organization;
import com.aeontronix.enhancedmule.tools.api.*;
import com.aeontronix.enhancedmule.tools.exchange.AssetInstance;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kloudtek.util.InvalidStateException;
import com.kloudtek.util.validation.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class APIDescriptor {
    private static final Logger logger = LoggerFactory.getLogger(APIDescriptor.class);
    private List<APIAccessDescriptor> access;
    private String assetId;
    private String assetVersion;
    private String endpoint;
    private List<PolicyDescriptor> policies;
    private List<String> accessedBy;
    private String label;
    private boolean createClientApplication = true;
    private ClientApplicationDescriptor clientApp;
    private List<SLATierDescriptor> slaTiers;

    public APIDescriptor() {
    }

    public APIDescriptor(String assetId, String version) {
        this.assetId = assetId;
        this.assetVersion = version;
    }

    public void provision(AnypointConfigFileDescriptor cfg, Environment environment, APIProvisioningConfig config, APIProvisioningResult result) throws HttpException, NotFoundException {
        ValidationUtils.notEmpty(IllegalStateException.class, "API Descriptor missing value: name", assetId);
        ValidationUtils.notEmpty(IllegalStateException.class, "API Descriptor missing value: version", assetVersion);
        logger.debug("Provisioning " + this + " within org " + environment.getParent().getName() + " env " + environment.getName());
        logger.debug("Provisioning " + this.getAssetId());
        String apiName = cfg.applyVars(this.getAssetId(), config);
        config.setVariable("api.name", apiName);
        config.setVariable("api.lname", apiName.toLowerCase());
        String apiVersionName = cfg.applyVars(this.getVersion(), config);
        if (clientApp == null) {
            clientApp = new ClientApplicationDescriptor();
        }
        String clientAppName = cfg.applyVars(clientApp.getName(), config);
        String endpoint = cfg.applyVars(this.getEndpoint(), config);
        String filteredLabel = cfg.applyVars(label,config);
        API api;
        try {
            api = environment.findAPIByExchangeAssetIdOrNameAndVersion(apiName, apiVersionName, filteredLabel);
            logger.debug("API " + apiName + " " + apiVersionName + " exists: " + api);
        } catch (NotFoundException e) {
            logger.debug("API " + apiName + " " + apiVersionName + " not found, creating");
            APISpec apiSpec = environment.getParent().findAPISpecsByIdOrNameAndVersion(this.getAssetId(), this.getVersion());
            // now we need to check if there's an existing API with the same productAPIVersion
            String productAPIVersion = apiSpec.getProductAPIVersion();
            try {
                api = environment.findAPIByExchangeAssetIdOrNameAndProductAPIVersion(apiName, productAPIVersion, filteredLabel);
                api = api.updateVersion(assetVersion);
            } catch (NotFoundException ex) {
                Boolean m3 = cfg.getMule3();
                if (m3 == null) {
                    m3 = false;
                }
                api = environment.createAPI(apiSpec, !m3, endpoint, filteredLabel != null ? filteredLabel : cfg.applyVars(config.getApiLabel(),config));
            }
        }
        result.setApi(api);
        if (policies != null) {
            api.deletePolicies();
            for (PolicyDescriptor policyDescriptor : policies) {
                api.createPolicy(policyDescriptor);
            }
        }
        ClientApplication clientApplication = null;
        try {
            clientApplication = environment.getOrganization().findClientApplicationByName(clientAppName);
            logger.debug("Client application found: " + clientAppName);
        } catch (NotFoundException e) {
            //
        }
        if (clientApplication == null && isCreateClientApplication()) {
            logger.debug("Client application not found, creating: " + clientAppName);
            clientApplication = environment.getOrganization().createClientApplication(clientAppName, clientApp.getUrl(), clientApp.getDescription());
        }
        result.setClientApplication(clientApplication);
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
        if (access != null) {
            if (clientApplication == null) {
                throw new InvalidStateException("Client Application doesn't exist and automatic client application creation (createClientApplication) set to false");
            }
            for (APIAccessDescriptor accessDescriptor : access) {
                AssetInstance instance = environment.getOrganization().getClient().findOrganizationById(accessDescriptor.getGroupId())
                        .findExchangeAsset(accessDescriptor.getGroupId(), accessDescriptor.getAssetId()).findInstances(accessDescriptor.getLabel());
                logger.debug("Found instance {}", instance);
                Environment apiEnv = new Environment(new Organization(environment.getClient(), instance.getOrganizationId()), instance.getEnvironmentId());
                API accessedAPI = new API(apiEnv);
                accessedAPI.setId(instance.getId());
                logger.debug("Found apiEnv {} with id {}", apiEnv, apiEnv.getId());
                APIContract contract;
                try {
                    contract = accessedAPI.findContract(clientApplication);
                } catch (NotFoundException e) {
                    SLATier slaTier = accessDescriptor.getSlaTier() != null ? accessedAPI.findSLATier(accessDescriptor.getSlaTier()) : null;
                    contract = clientApplication.requestAPIAccess(accessedAPI, slaTier);
                }
                if (!contract.isApproved() && config.isAutoApproveAPIAccessRequest()) {
                    if (contract.isRevoked()) {
                        contract.restoreAccess();
                    } else {
                        contract.approveAccess();
                    }
                }
            }
        }
    }

    @JsonProperty
    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    @JsonProperty
    public String getVersion() {
        return assetVersion;
    }

    public void setVersion(String version) {
        this.assetVersion = version;
    }

    @JsonProperty
    public synchronized List<APIAccessDescriptor> getAccess() {
        return access != null ? access : Collections.emptyList();
    }

    public synchronized void setAccess(List<APIAccessDescriptor> access) {
        this.access = access;
    }

    public synchronized APIDescriptor addAccess(APIAccessDescriptor accessDescriptor) {
        if (access == null) {
            access = new ArrayList<>();
        }
        access.add(accessDescriptor);
        return this;
    }

    public synchronized APIDescriptor addAccess(API api) {
        addAccess(new APIAccessDescriptor(api, null));
        return this;
    }

    public synchronized APIDescriptor addAccess(API api, String slaTier) {
        addAccess(new APIAccessDescriptor(api, slaTier));
        return this;
    }

    @JsonProperty
    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    @JsonProperty
    public ClientApplicationDescriptor getClientApp() {
        return clientApp;
    }

    public void setClientApp(ClientApplicationDescriptor clientApp) {
        this.clientApp = clientApp;
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

    @JsonProperty(defaultValue = "true")
    public boolean isCreateClientApplication() {
        return createClientApplication;
    }

    public void setCreateClientApplication(boolean createClientApplication) {
        this.createClientApplication = createClientApplication;
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
}
