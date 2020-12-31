/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIProvisioningConfig;
import com.aeontronix.enhancedmule.tools.provisioning.api.APIProvisioningResult;
import com.aeontronix.enhancedmule.tools.provisioning.api.ClientApplicationDescriptor;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class ApplicationProvisioningService {
    private static final Logger logger = getLogger(ApplicationProvisioningService.class);

    public APIProvisioningResult provision(ApplicationDescriptor applicationDescriptor, Environment environment,
                                           APIProvisioningConfig config, ApplicationSource source) throws ProvisioningException {
        try {
            APIProvisioningResult result = new APIProvisioningResult();
            final APIDescriptor api = applicationDescriptor.getApi();
            if (api != null) {
                logger.debug("API descriptor found, provisioning");
                api.provision(applicationDescriptor, environment, config, source, result);
            }
            final ClientApplicationDescriptor client = applicationDescriptor.getClient();
            if (client != null) {
                client.provision(applicationDescriptor, environment, config, result);
            }
            return result;
        } catch (Exception e) {
            throw new ProvisioningException(e);
        }
    }

}
