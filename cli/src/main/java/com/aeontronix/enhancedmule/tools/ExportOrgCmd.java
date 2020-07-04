/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.provisioning.OrganizationDescriptor;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;

@CommandLine.Command(name = "export-org", showDefaultValues = true, sortOptions = false,
        description = "Exports an organization as a JSON descriptor")
public class ExportOrgCmd extends AbstractOrganizationalCmd {
    @CommandLine.Option(names = {"-s", "--stripids"}, description = "Strip all ids")
    private boolean stripIds;

    @Override
    protected void execute(Organization organization) throws Exception {
        OrganizationDescriptor orgDesc = organization.export(stripIds);
        new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writerWithDefaultPrettyPrinter()
                .writeValue(System.out, orgDesc);
    }
}
