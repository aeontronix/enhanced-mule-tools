/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli.exchange;

import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.cli.AbstractCommand;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

@CommandLine.Command(name = "promote", description = "Promote an snapshot application in exchange to a release version")
public class ExchangePromoteApplicationCmd extends AbstractCommand implements Callable<Integer> {
    @CommandLine.Option(names = {"-g", "--group-id"}, description = "Business group name or id")
    private String businessGroup;
    @Parameters(index = "0", arity = "1", description = "Asset Id")
    private String assetId;
    @Parameters(index = "1", arity = "1", description = "Asset Version")
    private String version;
    @CommandLine.Option(names = {"-n", "--new-version"}, description = "New version")
    private String newVersion;

    @Override
    public Integer call() throws Exception {
        final EnhancedMuleClient emClient = getCli().createEMClient();
        final LegacyAnypointClient anypointClient = emClient.getLegacyAnypointClient();
        final Organization org;
        if (businessGroup != null) {
            org = anypointClient.findOrganizationByNameOrId(businessGroup);
        } else {
            org = anypointClient.getUser().getOrganization();
            org.setClient(anypointClient);
        }
        org.promoteExchangeApplication(emClient, org.getId(), assetId, version, newVersion);
        return 0;
    }
}
