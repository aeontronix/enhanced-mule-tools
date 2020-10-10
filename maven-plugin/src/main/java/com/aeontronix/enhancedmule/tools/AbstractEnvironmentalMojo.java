/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;

public abstract class AbstractEnvironmentalMojo extends AbstractOrganizationalMojo {
    private Environment environment;
    /**
     * Anypoint Environment name
     */
    @Parameter(property = "anypoint.env", required = true)
    protected String env;

    public synchronized Environment getEnvironment() throws NotFoundException, IOException {
        if (environment == null) {
            if (env == null) {
                throw new IllegalStateException("environment name (anypoint.env) is missing");
            }
            environment = getOrganization().findEnvironmentByName(env);
            getLog().debug("Using environment "+env+" : "+environment.getId());
        }
        return environment;
    }
}
