/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.anypoint.provisioning;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.application.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.APIProvisioningResult;
import com.aeontronix.enhancedmule.tools.application.api.ClientApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.application.api.MuleAPIProvisioningService;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationProvisioningService {
    private static final Logger logger = getLogger(ApplicationProvisioningService.class);
    private MuleAPIProvisioningService muleAPIProvisioningService;
    private LegacyAnypointClient client;

    public ApplicationProvisioningService(LegacyAnypointClient client, MuleAPIProvisioningService muleAPIProvisioningService) {
        this.muleAPIProvisioningService = muleAPIProvisioningService;
    }

    public ApplicationProvisioningService(LegacyAnypointClient client) {
        this(client, new MuleAPIProvisioningService(client));
    }

    public APIProvisioningResult provision(ApplicationDescriptor applicationDescriptor, Environment environment,
                                           ProvisioningRequest request) throws ProvisioningException {
        try {
            logger.info("Starting provisioning");
            APIProvisioningResult result = new APIProvisioningResult();
            final APIDescriptor api = applicationDescriptor.getApi();
            if (api != null) {
                logger.debug("API descriptor found, provisioning");
                muleAPIProvisioningService.provisionAPI(api, applicationDescriptor, environment, result);
            } else {
                logger.debug("No API description found");
            }
            final ClientApplicationDescriptor client = applicationDescriptor.getClient();
            if (client != null) {
                client.provision(applicationDescriptor, environment, request, result);
            }
            logger.info("Finished provisioning");
            return result;
        } catch (Exception e) {
            throw new ProvisioningException(e);
        }
    }
}
