/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.config.ProfileNotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import picocli.CommandLine.Option;

import java.io.IOException;

public class AbstractEnvCommand extends AbstractOrgCommand {
    @Option(names = {"-e", "--env"}, description = "Environment name or id")
    private String envNameOrId;
    private String envId;

    public String getEnvId() throws IOException, ProfileNotFoundException, NotFoundException {
        if (envId == null) {
            EMTCli cli = getCli();
            LegacyAnypointClient anypointClient = cli.getEMClient().getLegacyAnypointClient();
            if (StringUtils.isBlank(envNameOrId)) {
                envNameOrId = cli.getActiveProfile().getDefaultEnv();
            }
            if( envNameOrId == null ) {
                throw new IllegalArgumentException("Environment not specified in command line or in active configuration");
            }
            envId = anypointClient.findOrganizationById(getOrgId()).findEnvironmentByNameOrId(envNameOrId).getId();
        }
        return envId;
    }
}
