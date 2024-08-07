/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.anypoint.exchange;

/**
 * Created by JacksonGenerator on 6/26/18.
 */

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.exception.UnexpectedException;
import com.aeontronix.enhancedmule.tools.anypoint.AnypointObject;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.application.api.APICustomField;
import com.aeontronix.enhancedmule.tools.application.api.APICustomFieldDescriptor;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.slf4j.LoggerFactory.getLogger;


public class ExchangeAsset extends AnypointObject<Organization> {
    private static final Logger logger = getLogger(ExchangeAsset.class);
    @JsonProperty("productAPIVersion")
    private String productAPIVersion;
    @JsonProperty("runtimeVersion")
    private String runtimeVersion;
    @JsonProperty("metadata")
    private AssetMetadata metadata;
    @JsonProperty("instances")
    private List<AssetInstance> instances;
    @JsonProperty("modifiedAt")
    private String modifiedAt;
    @JsonProperty("groupId")
    private String groupId;
    @JsonProperty("rating")
    private Integer rating;
    @JsonProperty("type")
    private String type;
    @JsonProperty("createdAt")
    private String createdAt;
    @JsonProperty("generated")
    private List generated;
    @JsonProperty("assetId")
    private String assetId;
    @JsonProperty("versionGroup")
    private String versionGroup;
    @JsonProperty("minorVersion")
    private String minorVersion;
    @JsonProperty("permissions")
    private List<String> permissions;
    @JsonProperty("isPublic")
    private Boolean isPublic;
    @JsonProperty("categories")
    private List<AssetCategory> categories;
    @JsonProperty("id")
    private String id;
    @JsonProperty("assetLink")
    private String assetLink;
    @JsonProperty("version")
    private String version;
    @JsonProperty("labels")
    private List<String> labels;
    @JsonProperty("tags")
    private List<AssetTag> tags;
    @JsonProperty("dependencies")
    private List dependencies;
    @JsonProperty("createdBy")
    private AssetCreatedBy createdBy;
    @JsonProperty("versions")
    private List<AssetVersion> versions;
    @JsonProperty("otherVersions")
    private List<AssetVersion> otherVersions;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("files")
    private List<AssetFile> files;
    @JsonProperty("attributes")
    private List<AssetAttribute> attributes;
    @JsonProperty("status")
    private String status;
    @JsonProperty("numberOfRates")
    private Integer numberOfRates;
    @JsonProperty("icon")
    private String icon;
    @JsonProperty("customFields")
    private List<APICustomField> customFields;

    public ExchangeAsset() {
    }

    public ExchangeAsset(Organization organization) {
        super(organization);
    }

    public AssetInstance findInstances(@Nullable String label, String envId) throws NotFoundException {
        if (instances != null) {
            for (AssetInstance instance : instances) {
                instance.setParent(this);
            }
            Stream<AssetInstance> s = instances.stream().filter(i -> i.getEnvironmentId() != null && i.getEnvironmentId().equalsIgnoreCase(envId));
            boolean namedInstance = !StringUtils.isEmpty(label);
            if (namedInstance) {
                s = s.filter(i -> i.getName().equalsIgnoreCase(label));
            }
            List<AssetInstance> ilist = s.collect(Collectors.toList());
            if (ilist.isEmpty()) {
                String labelStr = label != null ? " with label '" + label + "'" : "";
                throw new NotFoundException("Can't find any instances for env " + envId + labelStr + " in asset " + assetId + ", please specify version number if you wish to use a different minor version than " + minorVersion + " or remove version to automatically find one that has a matching instance.");
            } else if (ilist.size() > 1) {
                if (namedInstance) {
                    throw new NotFoundException("Found more than one instance for api " + groupId + ":" + assetId + " while searching for instance " + label +
                            ". This is very unexpected as there shouldn't be instances with the same label");
                } else {
                    List<String> instanceNames = instances.stream().map(AssetInstance::getName).collect(Collectors.toList());
                    throw new NotFoundException("Found more than one instance for api " + groupId + ":" + assetId + ", please specify instance label: " + instanceNames);
                }
            } else {
                return ilist.iterator().next();
            }
        }
        throw new NotFoundException("Can't find asset " + label + " in env " + envId);
    }

    public String getPage(String name) throws HttpException, NotFoundException {
        try {
            return httpHelper.httpGet(new URLBuilder(getUrl()).path("/pages/").pathEl(name).toString(), Collections.singletonMap("Accept", "text/markdown"));
        } catch (HttpException e) {
            if (e.getStatusCode() == 404) {
                throw new NotFoundException("Page not found: " + name);
            } else {
                throw e;
            }
        }
    }

    public Portal getPortal() throws HttpException {
        final String json = httpHelper.httpGet(getUrl() + "/portal");
        return jsonHelper.readJson(new Portal(), json);
    }

    public ExchangeAsset updateLabels(List<String> exchangeTags) throws HttpException {
        httpHelper.httpPut(getUrl() + "/tags", exchangeTags.stream().map(t -> Collections.singletonMap("value", t)).collect(Collectors.toList()));
        try {
            return getParent().findExchangeAsset(groupId, assetId);
        } catch (NotFoundException e) {
            throw new UnexpectedException(e);
        }
    }

    public void updatePage(String name, String content) throws HttpException {
        httpHelper.httpPut(new URLBuilder(getUrl()).path("draft/pages").pathEl(name).toString(),
                Collections.singletonMap("Content-Type", "text/markdown"), content);
        httpHelper.httpPatch(getUrl(), null);
    }

    public void deleteCategory(String key) throws HttpException {
        httpHelper.httpDelete(new URLBuilder(getUrl()).path("tags/categories").pathEl(key).toString());
    }

    public void updateName(String name) throws HttpException {
        HashMap<String, String> req = new HashMap<>();
        req.put("name", name);
        httpHelper.httpPatch("/exchange/api/v2/assets/" + getParent().getId() + "/" + assetId, req);
    }

    public void updateDescription(String description) throws HttpException {
        HashMap<String, String> req = new HashMap<>();
        req.put("description", description);
        httpHelper.httpPatch("/exchange/api/v2/assets/" + getParent().getId() + "/" + assetId, req);
    }

    public void updateCategory(String key, List<String> catValues) throws HttpException {
        httpHelper.httpPut(new URLBuilder(getUrl()).path("tags/categories").pathEl(key).toString(),
                Collections.singletonMap("tagValue", catValues));
    }

    public CustomFieldUpdateResults updateCustomFields(List<APICustomFieldDescriptor> fields) throws HttpException {
        CustomFieldUpdateResults results = new CustomFieldUpdateResults();
        if (fields != null) {
            final List<APICustomFieldDescriptor> definedFields = new ArrayList<>(fields);
            final Map<String, Object> presentFields = customFields != null ?
                    customFields.stream().collect(Collectors.toMap(APICustomField::getKey, new Function<APICustomField, Object>() {
                        @Override
                        public Object apply(APICustomField f) {
                            return f.getValue();
                        }
                    })) :
                    new HashMap<>();
            for (APICustomFieldDescriptor f : definedFields) {
                final String key = f.getKey();
                final Object v = presentFields.remove(key);
                if (v == null || !v.equals(f.getValue())) {
                    try {
                        httpHelper.httpPut(new URLBuilder(getUrl()).path("tags/fields").pathEl(key).toString(),
                                new TagValueWrapper(f.getValue()));
                        results.modified.add(key);
                        logger.debug("Updated field {} to {}", key, f.getValue().toString());
                    } catch (HttpException e) {
                        if (e.getStatusCode() == 404 && !f.isRequired()) {
                            results.notDefined.add(key);
                            logger.debug("Unable to set custom field as it's not defined: " + key);
                        } else {
                            throw e;
                        }
                    }
                }
            }
            if (!presentFields.isEmpty()) {
                for (String key : presentFields.keySet()) {
                    httpHelper.httpDelete(new URLBuilder(getUrl()).path("tags/fields").path(key).toString());
                    results.modified.add(key);
                }
            }
        }
        return results;
    }

    @NotNull
    private String getUrl() {
        return "/exchange/api/v1/organizations/" + getParent().getId() + "/assets/" + groupId + "/" + assetId + "/" + version;
    }

    public String getProductAPIVersion() {
        return productAPIVersion;
    }

    public void setProductAPIVersion(String productAPIVersion) {
        this.productAPIVersion = productAPIVersion;
    }

    public String getRuntimeVersion() {
        return runtimeVersion;
    }

    public void setRuntimeVersion(String runtimeVersion) {
        this.runtimeVersion = runtimeVersion;
    }

    public AssetMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(AssetMetadata metadata) {
        this.metadata = metadata;
    }

    public List<AssetInstance> getInstances() {
        return instances;
    }

    public void setInstances(List<AssetInstance> instances) {
        this.instances = instances;
    }

    public String getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(String modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List getGenerated() {
        return generated;
    }

    public void setGenerated(List generated) {
        this.generated = generated;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getVersionGroup() {
        return versionGroup;
    }

    public void setVersionGroup(String versionGroup) {
        this.versionGroup = versionGroup;
    }

    public String getMinorVersion() {
        return minorVersion;
    }

    public void setMinorVersion(String minorVersion) {
        this.minorVersion = minorVersion;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }

    public Boolean getPublic() {
        return isPublic;
    }

    public void setPublic(Boolean aPublic) {
        isPublic = aPublic;
    }

    public List<AssetCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<AssetCategory> categories) {
        this.categories = categories;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAssetLink() {
        return assetLink;
    }

    public void setAssetLink(String assetLink) {
        this.assetLink = assetLink;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<AssetTag> getTags() {
        return tags;
    }

    public void setTags(List<AssetTag> tags) {
        this.tags = tags;
    }

    public List getDependencies() {
        return dependencies;
    }

    public void setDependencies(List dependencies) {
        this.dependencies = dependencies;
    }

    public AssetCreatedBy getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(AssetCreatedBy createdBy) {
        this.createdBy = createdBy;
    }

    @NotNull
    public List<AssetVersion> getVersions() {
        return versions != null ? versions : Collections.emptyList();
    }

    public void setVersions(List<AssetVersion> versions) {
        this.versions = versions;
        if (versions != null) {
            for (AssetVersion assetVersion : versions) {
                assetVersion.setParent(this);
            }
        }
    }

    public List<AssetVersion> getOtherVersions() {
        return otherVersions;
    }

    public void setOtherVersions(List<AssetVersion> otherVersions) {
        this.otherVersions = otherVersions;
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

    public List<AssetFile> getFiles() {
        return files;
    }

    public void setFiles(List<AssetFile> files) {
        this.files = files;
    }

    public List<AssetAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AssetAttribute> attributes) {
        this.attributes = attributes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getNumberOfRates() {
        return numberOfRates;
    }

    public void setNumberOfRates(Integer numberOfRates) {
        this.numberOfRates = numberOfRates;
    }

    public List<APICustomField> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(List<APICustomField> customFields) {
        this.customFields = customFields;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @JsonIgnore
    public byte[] getIconImage() throws HttpException {
        if (icon != null) {
            return httpHelper.httpGetBinary(icon);
        } else {
            return null;
        }
    }

    public void updateIcon(byte[] data, String mimeType) throws HttpException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", mimeType);
        httpHelper.httpPut(getExchangeAssetUrl().path("icon").toString(), headers, data);
    }

    public void deleteVersion(String version) throws HttpException {
        httpHelper.httpHardDelete(getExchangeAssetUrl().pathEl(version).toString());
    }

    private URLBuilder getExchangeAssetUrl() {
        return new URLBuilder("/exchange/api/v2/assets/").pathEl(getParent().getId(), assetId);
    }

    public static class TagValueWrapper {
        @JsonProperty
        private Object tagValue;

        public TagValueWrapper() {
        }

        public TagValueWrapper(Object tagValue) {
            this.tagValue = tagValue;
        }

        public Object getTagValue() {
            return tagValue;
        }

        public void setTagValue(String tagValue) {
            this.tagValue = tagValue;
        }
    }

    public class CustomFieldUpdateResults {
        List<String> modified = new ArrayList<>();
        List<String> notDefined = new ArrayList<>();

        public List<String> getModified() {
            return modified;
        }

        public List<String> getNotDefined() {
            return notDefined;
        }
    }
}
