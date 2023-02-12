/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli.exchange;

import com.aeontronix.anypointsdk.AnypointClient;
import com.aeontronix.anypointsdk.auth.user.User;
import com.aeontronix.anypointsdk.exchange.CreateExchangeAssetRequest;
import com.aeontronix.enhancedmule.tools.cli.AbstractCommand;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

@CommandLine.Command(name = "publish", aliases = "p", description = "Publish an exchange asset")
public class ExchangePublishAssetCmd extends AbstractCommand implements Callable<Integer> {
    private static final Logger logger = getLogger(ExchangePublishAssetCmd.class);
    @Option(names = {"-o", "--org-id"}, description = "Org Id")
    private String orgId;
    @Option(names = {"-u", "--group-id"}, description = "Group Id")
    private String groupId;
    @Option(names = {"-a", "--asset-id"}, description = "Asset Id")
    private String assetId;
    @Option(names = {"-v", "--version"}, description = "Version")
    private String version;
    @Option(names = {"-n", "--name"}, description = "Name")
    private String name;
    @Option(names = {"-d", "--description"}, description = "Description")
    private String description;
    @Option(names = {"-t", "--type"}, description = "Type")
    private String type;
    @Option(names = {"-p", "--dependency"}, description = "Dependencies")
    private List<String> dependencies;
    @Option(names = {"-w", "--keyword"}, description = "Keywords")
    private List<String> keywords;
    @Option(names = {"-g", "--tags"}, description = "Tags")
    private List<String> tags;
    @Option(names = {"-F"}, description = "Fields (use comma seperated list for multiple values)")
    private Map<String, String> fields;
    @Option(names = {"-C"}, description = "Categories (use comma seperated list for multiple values)")
    private Map<String, String> categories;
    @Option(names = {"-b", "--contact-name"}, description = "Categories (use comma seperated list for multiple values)")
    private String contactName;
    @Option(names = {"-e", "--contact-email"}, description = "Categories (use comma seperated list for multiple values)")
    private String contactEmail;
    @Option(names = {"-P", "--properties"}, description = "Properties")
    private Map<String, String> properties;
    @Option(names = {"-s", "--status"}, description = "Status ( Valid values: ${COMPLETION-CANDIDATES} )")
    private CreateExchangeAssetRequest.Status status = CreateExchangeAssetRequest.Status.PUBLISHED;
    @ArgGroup(exclusive = false)
    private List<AssetFile> files;

    @Override
    public Integer call() throws Exception {
        logger.info("Uploading asset to exchange");
        AnypointClient anypointClient = getCli().getAnypointClient();
        User user = null;
        if (orgId == null) {
            user = anypointClient.getUser().getUser();
        }
        if (assetId == null) {
            throw new IllegalArgumentException("assetId not set and couldn't be automatically identified");
        }
        if (version == null) {
            throw new IllegalArgumentException("version not set and couldn't be automatically identified");
        }
        CreateExchangeAssetRequest builder = anypointClient.getExchangeClient().createAsset()
                .orgId(orgId != null ? orgId : user.getOrganizationId()).groupId(groupId).assetId(assetId)
                .version(version).name(name != null ? name : assetId).description(description)
                .type(type).dependencies(dependencies).keywords(keywords).tags(tags).contactName(contactName)
                .contactEmail(contactEmail).properties(properties).status(status);
        if (fields != null) {
            builder = builder.fields(fields.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    e -> new HashSet<>(Arrays.asList(e.getValue().split(","))))));
        }
        if (categories != null) {
            builder = builder.categories(categories.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                    e -> new HashSet<>(Arrays.asList(e.getValue().split(","))))));
        }
        for (AssetFile assetFile : files) {
            String filename = assetFile.file.getName();
            if (assetFile.packaging == null) {
                int idx = filename.lastIndexOf(".");
                if (idx < 0) {
                    throw new IllegalArgumentException("Packaging not set and not extension found in file name");
                }
                assetFile.packaging = filename.substring(idx + 1);
            }
            if (assetFile.mimeType == null) {
                assetFile.mimeType = Files.probeContentType(assetFile.file.toPath());
            }
            if (assetFile.mimeType == null) {
                assetFile.mimeType = "application/octet-stream";
            }
            builder = builder.file(assetFile.packaging, assetFile.classifier, assetFile.mimeType, assetFile.file.getName(), assetFile.file);
        }
        builder.execute();
        logger.info("Asset uploaded successfully");
        return 0;
    }

    public static class AssetFile {
        @Option(names = "-f", description = "File path", required = true)
        File file;
        @Option(names = "-k", description = "File packaging", required = false)
        String packaging;
        @Option(names = "-c", description = "File classified", required = false)
        String classifier;
        @Option(names = "-m", description = "File mimeType", required = false)
        String mimeType;
    }
}
