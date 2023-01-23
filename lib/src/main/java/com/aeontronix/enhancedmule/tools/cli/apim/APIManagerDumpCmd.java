/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli.apim;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.api.API;
import com.aeontronix.enhancedmule.tools.anypoint.api.policy.Policy;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.StreamSupport;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "dump", description = "Dump the policies configuration JSON for an API", showDefaultValues = true)
public class APIManagerDumpCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(APIManagerDumpCmd.class);
    @ParentCommand
    private APIManagerCmd parent;
    @Option(names = "-o", description = "Organization group name or id")
    private String orgName;
    @Parameters(index = "1", arity = "0..1", description = "Only include APIs matching this regex pattern", defaultValue = ".*")
    private String regex;
    @Parameters(index = "0", arity = "1", description = "Environment name or id")
    private String envName;

    @Override
    public Integer call() throws Exception {
        final LegacyAnypointClient client = parent.getCli().createEMClient().getLegacyAnypointClient();
        final Organization org;
        if (orgName != null) {
            org = client.findOrganizationByNameOrId(this.orgName);
        } else {
            org = client.getUser().getOrganization();
            org.setClient(client);
        }
        final Environment env = org.findEnvironmentByNameOrId(envName);
        StreamSupport.stream(env.findAllAPIs().spliterator(), false).filter(v -> v.getName().matches(regex) ||
                v.getExchangeAssetName().matches(regex)).forEach(a -> {
            final List<API> apiList = a.getApis();
            if (apiList.size() == 0) {
                logger.warn("API has no API versions: " + a.getName());
            } else {
                try {
                    final API api = a.getApis().get(0);
                    final List<Policy> policies = api.findPolicies();
                    logger.info("API: " + a.getName() + " / " + a.getExchangeAssetName() + " with " + policies.size() + " policies");
                    if (apiList.size() > 1) {
                        logger.warn("Note: Multiple API versions found, using the first one in list (" + api.getAssetVersion() + ")");
                    }
                    for (Policy policy : policies) {
                        final ObjectMapper jsonMapper = client.getJsonHelper().getJsonMapper();
                        String prettyJson;
                        try {
                            prettyJson = jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonMapper.readValue(policy.getJson(), Object.class));
                        } catch (JsonProcessingException e) {
                            prettyJson = policy.getJson();
                        }
                        logger.info(prettyJson);
                    }
                } catch (HttpException e) {
                    logger.error("Failed to retrieve policies: " + e.getMessage(), e);
                }
            }
        });
        return 0;
    }
}
