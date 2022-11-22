/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli.apim;

import com.aeontronix.commons.exception.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.api.API;
import com.aeontronix.enhancedmule.tools.anypoint.api.APIAsset;
import com.aeontronix.enhancedmule.tools.anypoint.api.policy.Policy;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.List;
import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "dump", description = "Dump the policies configuration JSON for an API")
public class APIManagerDumpCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(APIManagerDumpCmd.class);
    @ParentCommand
    private APIManagerCmd parent;
    @CommandLine.Option(names = "-o", description = "Organization group name or id")
    private String orgName;
    @Parameters(index = "0", arity = "1", description = "Environment name or id")
    private String envName;
    @Parameters(index = "1", arity = "1", description = "API name")
    private String apiName;

    @Override
    public Integer call() throws Exception {
        final AnypointClient client = parent.getCli().getClient().getAnypointClient();
        final Organization org;
        if (orgName != null) {
            org = client.findOrganizationByNameOrId(this.orgName);
        } else {
            org = client.getUser().getOrganization();
            org.setClient(client);
        }
        final Environment env = org.findEnvironmentByNameOrId(envName);
        final APIAsset apiAsset = findAPI(env);
        if (apiAsset.getApis().size() > 1) {
            logger.warn("Multiple API versions found, using the first one in list");
        }
        final API api = apiAsset.getApis().get(0);
        final List<Policy> policies = api.findPolicies();
        logger.info("Found " + policies.size() + " policies");
        for (Policy policy : policies) {
            final ObjectMapper jsonMapper = client.getJsonHelper().getJsonMapper();
            logger.info(jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMapper.readValue(policy.getJson(), Object.class)));
        }
        return 0;
    }

    private APIAsset findAPI(Environment env) throws HttpException, NotFoundException {
        for (APIAsset api : env.findAllAPIs()) {
            if (api.getName().equalsIgnoreCase(apiName) || api.getExchangeAssetName().equalsIgnoreCase(apiName)) {
                return api;
            }
        }
        throw new NotFoundException("API not found: " + apiName);
    }
}
