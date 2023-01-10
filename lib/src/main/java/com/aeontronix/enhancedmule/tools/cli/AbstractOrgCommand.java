/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.config.ProfileNotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import picocli.CommandLine;

import java.io.IOException;

public class AbstractOrgCommand extends AbstractCommand {
    @CommandLine.Option(names = {"-o", "--org"}, description = "Environment name or id")
    private String orgNameOrId;
    private String orgId;

    public String getOrgId() throws IOException, ProfileNotFoundException, NotFoundException {
        if (orgId == null) {
            EMTCli cli = getCli();
            LegacyAnypointClient anypointClient = cli.getEMClient().getLegacyAnypointClient();
            if (StringUtils.isBlank(orgNameOrId)) {
                orgNameOrId = cli.getActiveProfile().getDefaultOrg();
            }
            if( orgNameOrId == null ) {
                orgId = anypointClient.getUser().getOrganization().getId();
            } else {
                orgId = anypointClient.findOrganizationByNameOrId(orgNameOrId).getId();
            }
        }
        return orgId;
    }
}
