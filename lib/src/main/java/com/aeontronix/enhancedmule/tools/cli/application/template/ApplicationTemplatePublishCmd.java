/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli.application.template;

import com.aeontronix.commons.ArchiveUtils;
import com.aeontronix.commons.TempFile;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.User;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.enhancedmule.tools.util.FileStreamSource;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import org.slf4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParentCommand;

import java.io.File;
import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "publish", mixinStandardHelpOptions = true)
public class ApplicationTemplatePublishCmd implements Callable<Integer> {
    private static final Logger logger = getLogger(ApplicationTemplatePublishCmd.class);
    public static final String EMT_TEMPLATE_ASSET_ID = "emt-application-template";
    @Option(names = "-d", description = "Directory where template files will be created")
    private File directory;
    @Option(names = "-o", description = "Organization used to publish template")
    private String organization;
    @Option(names = "-i", description = "Exchange asset id")
    private String assetId = EMT_TEMPLATE_ASSET_ID;
    @Option(names = "-n", description = "Exchange asset name")
    private String assetName = "Enhanced Mule Application Template";
    @Option(names = "-e", description = "Exchange asset version")
    private String assetVersion = "0.0.1";
    @ParentCommand
    private ApplicationTemplateCmd parent;

    public ApplicationTemplatePublishCmd() {
    }

    @Override
    public Integer call() throws Exception {
        if (!directory.exists()) {
            logger.error("Directory not found: " + directory.getPath());
            return -1;
        }
        try (final TempFile tarch = new TempFile("tarch",".zip")) {
            ArchiveUtils.zipDir(directory.getPath(), tarch);
            final EnhancedMuleClient client = parent.getParent().getCli().getClient(organization, null);
            final Organization org = parent.getParent().getCli().findOrganization(organization);
            org.getClient().findOrganizations();// needed to force load creds
//            try {
//                final ExchangeAsset existingAsset = org.findExchangeAsset(org.getId(), assetId);
//            } catch (NotFoundException e) {
//                //
//            }
            org.publishExchangeCustomAsset(assetId, assetName, assetVersion, new FileStreamSource(tarch));
        }
        logger.info("Asset published successfully");
        return 0;
    }
}
