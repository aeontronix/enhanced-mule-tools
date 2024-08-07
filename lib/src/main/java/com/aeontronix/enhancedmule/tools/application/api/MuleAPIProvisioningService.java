/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.application.api;

import com.aeontronix.commons.validation.ValidationUtils;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.api.API;
import com.aeontronix.enhancedmule.tools.anypoint.api.APISpec;
import com.aeontronix.enhancedmule.tools.anypoint.api.ClientApplication;
import com.aeontronix.enhancedmule.tools.anypoint.api.SLATier;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAssetDescriptor;
import com.aeontronix.enhancedmule.tools.anypoint.provisioning.ProvisioningException;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

public class MuleAPIProvisioningService {
    private static final Logger logger = getLogger(MuleAPIProvisioningService.class);
    private static final EMTLogger plogger = new EMTLogger(logger);
    private LegacyAnypointClient client;

    public MuleAPIProvisioningService(LegacyAnypointClient client) {
        this.client = client;
    }

    public void provisionAPI(APIDescriptor apiDescriptor, ApplicationDescriptor cfg, Environment environment,
                             APIProvisioningResult result) throws ProvisioningException {
        try {
            final ExchangeAssetDescriptor asset = apiDescriptor.getAsset();
            ValidationUtils.notNull(IllegalStateException.class, "API Descriptor missing value: asset", asset);
            final String label = apiDescriptor.getLabel();
            final String consumerUrl = apiDescriptor.getConsumerUrl();
            final Organization organization = environment.getParent();
            final Map<String, Object> implementationUrlJson = apiDescriptor.getImplementationUrlJson();
            final String implementationUrl = apiDescriptor.getImplementationUrl();
            final String assetId = asset.getId();
            ValidationUtils.notNull(IllegalStateException.class, "API asset id cannot be null", assetId);
            final String assetVersion = asset.getVersion();
            ValidationUtils.notNull(IllegalStateException.class, "API asset version cannot be null", assetVersion);
            logger.info(Ansi.ansi().a("Provisioning ").fgGreen().a(assetId).reset().a(":").fgGreen().a(assetVersion)
                    .reset().a(" within org ").fgGreen().a(organization.getName()).reset().a(", env ").fgGreen()
                    .a(environment.getName()).toString());
            Boolean m3 = cfg.getMule3();
            if (m3 == null) {
                m3 = false;
            }
            API api;
            try {
                api = environment.findAPIByExchangeAssetIdOrNameAndVersion(assetId, assetVersion, label);
                logger.debug("API " + assetId + " " + assetVersion + " exists: " + api);
            } catch (NotFoundException e) {
                logger.debug("API " + assetId + " " + assetVersion + " not found, creating");
                APISpec apiSpec = organization.findAPISpecsByIdOrNameAndVersion(assetId, assetVersion);
                // now we need to check if there's an existing API with the same productAPIVersion
                String productAPIVersion = apiSpec.getProductAPIVersion();
                try {
                    logger.debug("findAPIByExchangeAssetIdOrNameAndProductAPIVersion: {} , {} , {}", assetId, productAPIVersion, label);
                    api = environment.findAPIByExchangeAssetIdOrNameAndProductAPIVersion(assetId, productAPIVersion, label);
                    final String currentAssetVersion = api.getAssetVersion();
                    if (!currentAssetVersion.equalsIgnoreCase(assetVersion)) {
                        api.updateVersion(assetVersion);
                        plogger.info(EMTLogger.Product.API_MANAGER, "Updated api {} version to {}", assetId, assetVersion);
                    }
                } catch (NotFoundException ex) {
                    logger.debug("Creating API");
                    if (implementationUrlJson != null) {
                        api = environment.createAPI(apiSpec, label, implementationUrlJson, consumerUrl);
                    } else {
                        api = environment.createAPI(apiSpec, !m3, implementationUrl, consumerUrl, label, asset.getType());
                    }
                    plogger.info(EMTLogger.Product.API_MANAGER, "Created api {}", api.getAssetId(), assetVersion);
                }
            }
            final List<PolicyDescriptor> policies = apiDescriptor.getPolicies();
            if (policies != null && !policies.isEmpty()) {
                plogger.info(EMTLogger.Product.API_MANAGER, "Setting policies for {}: {}", api.getAssetId(), policies.stream().map(PolicyDescriptor::getAssetId).collect(Collectors.joining(", ")));
                api.deletePolicies();
                for (PolicyDescriptor policyDescriptor : policies) {
                    logger.info("Creating policy {}:{}:{}", policyDescriptor.getGroupId(), policyDescriptor.getAssetId(), policyDescriptor.getAssetVersion());
                    api.createPolicy(policyDescriptor);
                }
            }
            final List<SLATierDescriptor> slaTiers = apiDescriptor.getSlaTiers();
            if (slaTiers != null && !slaTiers.isEmpty()) {
                plogger.info(EMTLogger.Product.API_MANAGER, "Setting SLA Tiers for {}", api.getAssetId());
                for (SLATierDescriptor slaTierDescriptor : slaTiers) {
                    try {
                        SLATier slaTier = api.findSLATier(slaTierDescriptor.getName());
                        slaTier.setAutoApprove(slaTierDescriptor.isAutoApprove());
                        slaTier.setDescription(slaTierDescriptor.getDescription());
                        slaTier.setLimits(slaTierDescriptor.getLimits());
                        plogger.info(EMTLogger.Product.API_MANAGER, "Updating SLA Tiers {}", slaTierDescriptor.getName());
                        slaTier = slaTier.update();
                    } catch (NotFoundException e) {
                        plogger.info(EMTLogger.Product.API_MANAGER, "Creating SLA Tiers {}", slaTierDescriptor.getName());
                        api.createSLATier(slaTierDescriptor.getName(), slaTierDescriptor.getDescription(), slaTierDescriptor.isAutoApprove(), slaTierDescriptor.getLimits());
                    }
                }
            }
            if (consumerUrl != null) {
                updateConsumerUrl(api, consumerUrl);
                plogger.info(EMTLogger.Product.API_MANAGER, "Updated consumer url to {}", consumerUrl);
            }
            if (implementationUrlJson != null) {
                api.updateImplementationUrl(implementationUrlJson);
                plogger.info(EMTLogger.Product.API_MANAGER, "Updated implementation url to {}", implementationUrlJson.toString());
            } else if (implementationUrl != null) {
                api.updateImplementationUrl(implementationUrl, !m3, asset.getType());
                plogger.info(EMTLogger.Product.API_MANAGER, "Updated implementation url to {}", implementationUrl);
            }
            if (apiDescriptor.getAccessedBy() != null) {
                for (String accessedBy : apiDescriptor.getAccessedBy()) {
                    ClientApplication clientApp = environment.getOrganization().getRootOrganization().findClientApplicationByName(accessedBy);
                    if (clientApp == null) {
                        throw new ProvisioningException("Client application not found: " + accessedBy);
                    }
                }
            }
            api = api.refresh();
            result.setApi(api);
            if (logger.isDebugEnabled()) {
                logger.debug("api: {}", api.toString());
            }
            // exchange
            asset.provision(environment.getOrganization());
        } catch (NotFoundException | IOException e) {
            throw new ProvisioningException(e);
        }
    }

    public void updateConsumerUrl(API api, String consumerUrl) throws HttpException {
        HashMap<String, Object> data = new HashMap<>();
        data.put("endpointUri", consumerUrl);
        client.getHttpHelper().httpPatch(api.getUrl(), data);
    }

}
