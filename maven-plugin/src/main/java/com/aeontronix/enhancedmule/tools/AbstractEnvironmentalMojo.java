/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.config.ProfileNotFoundException;
import com.aeontronix.enhancedmule.tools.util.EMTProperties;
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
    @Parameter
    protected String env;

    public synchronized Environment getEnvironment() throws NotFoundException, IOException, ProfileNotFoundException {
        if (environment == null) {
            env = getEnvProperty();
            if (env == null) {
                env = configProfile.getDefaultEnv();
                if (env == null) {
                    throw new IllegalStateException("Environment not set, please set using emt.env or set a default environment in your configuration profile");
                }
            }
            environment = getOrganization().findEnvironmentByNameOrId(env);
        }
        return environment;
    }

    protected String getEnvProperty() {
        return getMavenProperty("emt.env", env, "anypoint.env");
    }

    @Override
    public EMTProperties getEMTProperties() throws NotFoundException, IOException, ProfileNotFoundException {
        if (StringUtils.isNotBlank(getEnvProperty())) {
            getEnvironment();
            return new EMTProperties(getMavenProperties(), environment.getId(), environment.getName(), environment.getType());
        } else {
            return new EMTProperties(getMavenProperties(), null, null, null);
        }
    }
}
