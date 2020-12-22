/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.exchange;

import com.aeontronix.commons.FileUtils;
import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.TempFile;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetCategory;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetCreationException;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.api.API;
import com.aeontronix.enhancedmule.tools.provisioning.api.APICustomFieldDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.api.IconDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.portal.PortalDescriptor;
import com.aeontronix.enhancedmule.tools.provisioning.portal.PortalPageDescriptor;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import org.slf4j.Logger;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.stream.Collectors.toMap;
import static org.slf4j.LoggerFactory.getLogger;

public class ExchangeAssetDescriptor {
    public static final Pattern MAJOR_VERSION_PATTERN = Pattern.compile("^(\\d*)");
    private static final Logger logger = getLogger(ExchangeAssetDescriptor.class);
    private static final EMTLogger plogger = new EMTLogger(logger);
    private String groupId;
    @NotBlank
    private String id;
    @NotBlank
    private String version;
    @NotBlank
    private String apiVersion;
    private String name;
    private String description;
    private List<String> tags;
    private PortalDescriptor portal;
    private Map<String, List<String>> categories;
    private List<APICustomFieldDescriptor> fields;
    private IconDescriptor icon;
    private Boolean create;
    @NotNull
    private API.Type type = API.Type.REST;
    @NotBlank
    private String assetMainFile;

    public static String getMajorVersion(String version) {
        final Matcher m = MAJOR_VERSION_PATTERN.matcher(version);
        if (m.find()) {
            return m.group(1);
        } else {
            return null;
        }
    }

    public static File findIcon(File basedir) {
        for (String fn : Arrays.asList("icon.svg", "icon.png", "icon.jpeg", "icon.jpg", "icon.gif")) {
            final File f = new File(basedir, fn);
            if( f.exists() ) {
                return f;
            }
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public IconDescriptor getIcon() {
        return icon;
    }

    public void setIcon(IconDescriptor icon) {
        this.icon = icon;
    }

    public API.Type getType() {
        return type;
    }

    public void setType(API.Type type) {
        this.type = type;
    }

    public PortalDescriptor getPortal() {
        return portal;
    }

    public void setPortal(PortalDescriptor portal) {
        this.portal = portal;
    }

    public String getAssetMainFile() {
        return assetMainFile;
    }

    public void setAssetMainFile(String assetMainFile) {
        this.assetMainFile = assetMainFile;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public Map<String, List<String>> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, List<String>> categories) {
        this.categories = categories;
    }

    public List<APICustomFieldDescriptor> getFields() {
        return fields;
    }

    public void setFields(List<APICustomFieldDescriptor> fields) {
        this.fields = fields;
    }

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

    public Boolean getCreate() {
        return create;
    }

    public void setCreate(Boolean create) {
        this.create = create;
    }

    public void updateTags(ExchangeAsset exchangeAsset) throws HttpException {
        if (this.tags != null) {
            List<String> current = exchangeAsset.getLabels();
            List<String> expectedTags = this.tags;
            if (!current.equals(expectedTags)) {
                exchangeAsset = exchangeAsset.updateLabels(expectedTags);
                plogger.info(EMTLogger.Product.EXCHANGE, "Updated tags of {} : {}", exchangeAsset.getAssetId(), expectedTags);
            }
        }
    }

    public void create(Organization organization, APISpecSource apiSpecSource) throws AssetCreationException {
        try {
            if (type == API.Type.HTTP) {
                organization.createExchangeHTTPAPIAsset(null, name, id, version, apiVersion);
                plogger.info(EMTLogger.Product.EXCHANGE, "Created HTTP asset : {} : {} : {}", id, version, apiVersion);
            } else {
                if (StringUtils.isBlank(assetMainFile)) {
                    throw new AssetCreationException("assetMainFile is required for API asset creation");
                }
                String assetClassifier = getClassifier();
                try (TempFile apiSpecFile = new TempFile(id + "-" + version, ".zip")) {
                    final Set<String> files = apiSpecSource.listAPISpecFiles();
                    if (!files.contains(assetMainFile)) {
                        throw new IOException("asset main file not found: " + assetMainFile);
                    }
                    try (ZipOutputStream os = new ZipOutputStream(new FileOutputStream(apiSpecFile))) {
                        for (String file : files) {
                            os.putNextEntry(new ZipEntry(file));
                            apiSpecSource.writeAPISpecFile(file, os);
                            os.closeEntry();
                        }
                    }
                    organization.publishExchangeAPIAsset(name, id,
                            version, apiVersion, assetClassifier, assetMainFile, apiSpecFile);
                    plogger.info(EMTLogger.Product.EXCHANGE, "Created API asset : {} : {} : {}", id, version, apiVersion);
                }
            }
        } catch (IOException e) {
            throw new AssetCreationException(e);
        }
    }

    public String getClassifier() {
        return assetMainFile != null ? (assetMainFile.toLowerCase().endsWith(".raml") ? "raml" : "oas") : null;
    }

    public void provision(Organization organization) throws IOException, NotFoundException {
        ExchangeAsset exchangeAsset = organization.findExchangeAsset(groupId != null ? groupId : organization.getId(), id);
        if (icon != null && icon.getPath() != null) {
            File iconFile = new File(icon.getPath());
            if (!iconFile.exists()) {
                throw new IOException("Unable to find icon file: " + iconFile.getPath());
            }
            final Path path = iconFile.toPath();
            String mimeType = Files.probeContentType(path);
            if (StringUtils.isBlank(mimeType)) {
                String fpath = path.toString().toLowerCase();
                if (fpath.endsWith(".png")) {
                    mimeType = "image/png";
                } else if (fpath.endsWith(".svg")) {
                    mimeType = "image/svg+xml";
                } else if (fpath.endsWith(".gif")) {
                    mimeType = "image/gif";
                } else if (fpath.endsWith(".jpg") || fpath.endsWith(".jpeg")) {
                    mimeType = "image/jpg";
                }
            }
            if (StringUtils.isNotBlank(mimeType)) {
                icon.setMimeType(mimeType);
            } else {
                throw new IOException("Unable to identity mime-Type of icon image, please specify mimeType in descriptor: " + icon.getPath());
            }
            icon.setContent(StringUtils.base64Encode(FileUtils.toByteArray(iconFile)));
            icon.setPath(null);
        }
        if (name != null && !name.equals(exchangeAsset.getName())) {
            exchangeAsset.updateName(name);
            plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' name", exchangeAsset.getAssetId());
        }
        if (description != null && !description.equals(exchangeAsset.getDescription())) {
            exchangeAsset.updateDescription(description);
            plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' description", exchangeAsset.getAssetId());
        }
        updateTags(exchangeAsset);
        if (icon != null) {
            exchangeAsset.updateIcon(StringUtils.base64Decode(icon.getContent()), icon.getMimeType());
            plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' icon", exchangeAsset.getAssetId());
        }
        final ExchangeAsset.CustomFieldUpdateResults results = exchangeAsset.updateCustomFields(fields);
        for (String field : results.getModified()) {
            plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' custom field '{}'", exchangeAsset.getAssetId(), field);
        }
        for (String field : results.getNotDefined()) {
            logger.warn("Custom field not defined, assignment failed: " + field);
        }
        updateExchangeCategories(exchangeAsset);
        // portal
        if (portal != null) {
            portal.provision(exchangeAsset);
        }
    }

    private void updateExchangeCategories(ExchangeAsset exchangeAsset) throws HttpException {
        if (categories != null) {
            final Map<String, List<String>> assetCategories = exchangeAsset.getCategories().stream().collect(
                    toMap(AssetCategory::getKey, AssetCategory::getValue));
            for (String curCatKey : assetCategories.keySet()) {
                if (!categories.containsKey(curCatKey)) {
                    exchangeAsset.deleteCategory(curCatKey);
                    plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' category '{}'", exchangeAsset.getAssetId(), curCatKey);
                }
            }
            for (Map.Entry<String, List<String>> catEntries : categories.entrySet()) {
                List<String> catValues = catEntries.getValue() != null ? catEntries.getValue() : Collections.emptyList();
                final String catKey = catEntries.getKey();
                List<String> assetCatValues = assetCategories.getOrDefault(catKey, Collections.emptyList());
                if (!catValues.equals(assetCatValues)) {
                    exchangeAsset.updateCategory(catKey, catValues);
                    plogger.info(EMTLogger.Product.EXCHANGE, "Updated exchange asset '{}' category '{}' to '{}'", exchangeAsset.getAssetId(), catKey, catValues);
                }
            }
        }
    }

    public String getMajorVersion() {
        return getMajorVersion(version);
    }

    public void findPages(File assetPagesDir) throws IOException {
        if (assetPagesDir.exists() && assetPagesDir.isDirectory() ) {
            final File[] files = assetPagesDir.listFiles();
            if( files != null && files.length > 0 ) {
                if( portal == null ) {
                    portal = new PortalDescriptor();
                }
                List<PortalPageDescriptor> pages = portal.getPages();
                if( pages == null ) {
                    pages = new ArrayList<>();
                    portal.setPages(pages);
                }
                for (File file : files) {
                    if(file.isFile()) {
                        final String fileName = file.getName();
                        int idx = fileName.indexOf(".");
                        if( idx != -1 ) {
                            final PortalPageDescriptor p = new PortalPageDescriptor();
                            p.setContent(IOUtils.toString(file));
                            p.setName(fileName.substring(0,idx));
                            pages.add(p);
                        }
                    }
                }
            }
        }
    }
}
