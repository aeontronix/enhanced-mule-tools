/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.api.provision;

import com.aeontronix.enhancedmule.tools.Environment;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.NotFoundException;
import com.aeontronix.enhancedmule.tools.Organization;
import com.aeontronix.enhancedmule.tools.api.*;
import com.aeontronix.enhancedmule.tools.exchange.AssetInstance;
import com.aeontronix.enhancedmule.tools.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.util.UnauthorizedHttpException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kloudtek.util.InvalidStateException;
import com.kloudtek.util.StringUtils;
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

    public void provision(AnypointDescriptor cfg, Environment environment, APIProvisioningConfig config, APIProvisioningResult result) throws HttpException, NotFoundException {
        ValidationUtils.notEmpty(IllegalStateException.class, "API Descriptor missing value: assetId", assetId);
        ValidationUtils.notEmpty(IllegalStateException.class, "API Descriptor missing value: assetVersion", assetVersion);
        logger.debug("Provisioning " + this + " within org " + environment.getParent().getName() + " env " + environment.getName());
        logger.debug("Provisioning " + this.getAssetId());
        if (clientApp == null) {
            clientApp = new ClientApplicationDescriptor();
        }
        Boolean m3 = cfg.getMule3();
        if (m3 == null) {
            m3 = false;
        }
        API api;
        try {
            api = environment.findAPIByExchangeAssetIdOrNameAndVersion(this.getAssetId(), this.getAssetVersion(), label);
            logger.debug("API " + this.getAssetId() + " " + this.getAssetVersion() + " exists: " + api);
        } catch (NotFoundException e) {
            logger.debug("API " + this.getAssetId() + " " + this.getAssetVersion() + " not found, creating");
            APISpec apiSpec = environment.getParent().findAPISpecsByIdOrNameAndVersion(this.getAssetId(), this.getAssetVersion());
            // now we need to check if there's an existing API with the same productAPIVersion
            String productAPIVersion = apiSpec.getProductAPIVersion();
            try {
                logger.debug("findAPIByExchangeAssetIdOrNameAndProductAPIVersion: {} , {} , {}", this.getAssetId(), productAPIVersion, label);
                api = environment.findAPIByExchangeAssetIdOrNameAndProductAPIVersion(this.getAssetId(), productAPIVersion, label);
                api = api.updateVersion(assetVersion);
            } catch (NotFoundException ex) {
                logger.debug("Couldn't find, creating");
                api = environment.createAPI(apiSpec, !m3, this.getEndpoint(), label);
            }
        }
        if (this.getEndpoint() != null) {
            api = api.updateEndpoint(this.getEndpoint(), !m3);
        }
        result.setApi(api);
        if (policies != null) {
            api.deletePolicies();
            for (PolicyDescriptor policyDescriptor : policies) {
                api.createPolicy(policyDescriptor);
            }
        }
        if (clientApp.getName() == null) {
            clientApp.setName(api.getAssetId() + "-" + config.getVariables().get("organization.lname") + "-" + config.getVariables().get("environment.lname"));
        }
        ClientApplication clientApplication = null;
        try {
            clientApplication = environment.getOrganization().findClientApplicationByName(clientApp.getName());
            logger.debug("Client application found: " + clientApp.getName());
        } catch (NotFoundException e) {
            //
        }
        if (clientApplication == null && isCreateClientApplication()) {
            logger.debug("Client application not found, creating: " + clientApp.getName());
            clientApplication = environment.getOrganization().createClientApplication(clientApp.getName(), clientApp.getUrl(), clientApp.getDescription());
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
                logger.debug("Processing access descriptor : {}",accessDescriptor);
                Organization accessOrg;
                if (accessDescriptor.getOrgId() == null) {
                    logger.debug("Access descriptor has no org id, getting the default org");
                    accessOrg = environment.getOrganization();
                    accessDescriptor.setOrgId(accessOrg.getId());
                } else {
                    accessOrg = environment.getOrganization().getClient().findOrganizationById(accessDescriptor.getOrgId());
                }
                logger.debug("Access org = {}",accessOrg.getId());
                if (accessDescriptor.getGroupId() == null) {
                    logger.debug("No group id found, using the org id instead");
                    accessDescriptor.setGroupId(accessOrg.getId());
                }
                logger.debug("Access group id = {}",accessDescriptor.getGroupId());
                String accessEnvId;
                if( accessDescriptor.getEnvId() != null ) {
                    logger.debug("Env id set: {}",accessDescriptor.getEnvId());
                    accessEnvId = accessDescriptor.getEnvId();
                } else if(StringUtils.isNotBlank(accessDescriptor.getEnv()) ) {
                    accessEnvId = accessOrg.findEnvironmentByName(accessDescriptor.getEnv()).getId();
                    logger.debug("access environment specified");
                } else {
                    logger.debug("No access environment specified, using the API's environment");
                    accessEnvId = environment.getId();
                }
                logger.debug("Access environment id = {}",accessEnvId);
                ExchangeAsset exchangeAsset = accessOrg.findExchangeAsset(accessDescriptor.getGroupId(), accessDescriptor.getAssetId());
                logger.debug("Found exchangeAsset {}", exchangeAsset);
                logger.debug("exchangeAsset instances: {}", exchangeAsset.getInstances());
                AssetInstance instance = exchangeAsset.findInstances(accessDescriptor.getLabel(), accessEnvId);
                logger.debug("Found instance {}", instance);
                Environment apiEnv = new Environment(new Organization(environment.getClient(), instance.getOrganizationId()), instance.getEnvironmentId());
                API accessedAPI = new API(apiEnv);
                accessedAPI.setId(instance.getId());
                logger.debug("Found apiEnv {} with id {}", apiEnv, apiEnv.getId());
                APIContract contract = null;
                try {
                    contract = accessedAPI.findContract(clientApplication);
                } catch (NotFoundException e) {
                    //
                } catch( UnauthorizedHttpException e ) {
                    logger.warn("Unable to List contracts of api "+accessedAPI.getAssetId()+" due to lack of permissions: "+e.getMessage());
                }
                if( contract == null ) {
                    SLATier slaTier = null;
                    if( accessDescriptor.getSlaTier() != null ) {
                        slaTier = instance.findSLATier(accessDescriptor.getSlaTier());
                    } else {
                        List<SLATier> slaTiers = instance.findSLATiers();
                        if( slaTiers.size() == 1 ) {
                            slaTier = instance.findSLATier(slaTiers.iterator().next().getName());
                        }
                    }
                    contract = clientApplication.requestAPIAccess(accessedAPI, instance, slaTier);
                }
                if (!contract.isApproved() && config.isAutoApproveAPIAccessRequest()) {
                    try {
                        if (contract.isRevoked()) {
                            contract.restoreAccess();
                        } else {
                            contract.approveAccess();
                        }
                    } catch (HttpException e) {
                        if (e.getStatusCode() == 403) {
                            logger.warn("Unable to approve access to "+api.getAssetId()+" due to lack of permissions: "+e.getMessage());
                        } else {
                            throw e;
                        }
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
    public String getAssetVersion() {
        return assetVersion;
    }

    public void setAssetVersion(String assetVersion) {
        this.assetVersion = assetVersion;
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
