/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning;

import com.aeontronix.enhancedmule.tools.Environment;
import com.aeontronix.enhancedmule.tools.NotFoundException;
import com.aeontronix.enhancedmule.tools.Organization;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.kloudtek.util.StringUtils;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class EnvironmentDescriptor {
    private static final Logger logger = getLogger(EnvironmentDescriptor.class);
    private String name;
    private String description;
    private Environment.Type type;

    public Environment provision(Organization org) throws HttpException, ProvisioningException {
        Environment env;
        try {
            if(StringUtils.isBlank(name) ) {
                throw new ProvisioningException("environment name missing");
            }
            env = org.findEnvironmentByName(name);
        } catch (NotFoundException e) {
            env = org.createEnvironment(name, type);
            logger.info("Created environment {}", name);
        }
        return env;
    }

    @JsonProperty(required = false)
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

    @JsonProperty(required = true)
    public Environment.Type getType() {
        return type;
    }

    public void setType(Environment.Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "EnvironmentDescriptor{" +
                "name='" + name + '\'' +
                ", type=" + type +
                '}';
    }
}
