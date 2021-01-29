/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.provisioning;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.ApplicationDescriptor;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.api.*;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationProvisioningService {
    private static final Logger logger = getLogger(ApplicationProvisioningService.class);
    private MuleAPIProvisioningService muleAPIProvisioningService;
    private AnypointClient client;

    public ApplicationProvisioningService(AnypointClient client, MuleAPIProvisioningService muleAPIProvisioningService) {
        this.muleAPIProvisioningService = muleAPIProvisioningService;
    }

    public ApplicationProvisioningService(AnypointClient client) {
        this(client, new MuleAPIProvisioningService(client));
    }

    public APIProvisioningResult provision(ApplicationDescriptor applicationDescriptor, Environment environment,
                                           ProvisioningRequest request) throws ProvisioningException {
        try {
            APIProvisioningResult result = new APIProvisioningResult();
            final APIDescriptor api = applicationDescriptor.getApi();
            if (api != null) {
                logger.debug("API descriptor found, provisioning");
                muleAPIProvisioningService.provisionAPI(api, applicationDescriptor, environment, result);
            }
            final ClientApplicationDescriptor client = applicationDescriptor.getClient();
            if (client != null) {
                client.provision(applicationDescriptor, environment, request, result);
            }
            return result;
        } catch (Exception e) {
            throw new ProvisioningException(e);
        }
    }

}
