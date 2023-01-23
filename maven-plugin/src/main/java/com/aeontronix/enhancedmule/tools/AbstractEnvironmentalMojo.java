/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.config.ProfileNotFoundException;
import org.apache.maven.plugins.annotations.Parameter;
import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

public abstract class AbstractEnvironmentalMojo extends AbstractOrganizationalMojo {
    private static final Logger logger = getLogger(AbstractEnvironmentalMojo.class);
    private Environment environment;
    /**
     * Anypoint Environment name
     */
    @Parameter(property = "anypoint.env")
    protected String env;

    public synchronized Environment getEnvironment() throws NotFoundException, IOException, ProfileNotFoundException {
        if (environment == null) {
            if (env == null) {
                env = configProfile.getDefaultEnv();
                if (env == null) {
                    throw new IllegalStateException("Environment not set, using configuration element 'env', or maven property 'anypoint.env' to set");
                }
            }
            environment = getOrganization().findEnvironmentByNameOrId(env);
        }
        return environment;
    }
}
