/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import com.aeontronix.enhancedmule.tools.anypoint.application.descriptor.ApplicationDescriptor;
import com.aeontronix.kryptotek.CryptoUtils;
import com.aeontronix.kryptotek.EncodedKey;
import com.aeontronix.kryptotek.key.AESKey;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Iterator;

public abstract class AbstractCryptoMojo extends AbstractMojo {
    @Parameter(property = "anypoint.descriptor")
    private String descriptor;
    @Parameter(property = "key")
    private String key;
    @Parameter(property = "org")
    private String org;
    @Parameter(property = "profile")
    private String profile;
    private File anypointFile;
    protected ObjectMapper objectMapper;

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            if (key == null) {
                final EMConfig configFile = EMConfig.findConfigFile();
                final ConfigProfile configProfile = configFile.getProfileByProfileName(profile);
                key = configProfile.getCryptoKey();
                if (key == null) {
                    throw new MojoExecutionException("Key must be set or exist in configuration profile");
                }
            }
            final AESKey key = CryptoUtils.readKey(AESKey.class, new EncodedKey(this.key, EncodedKey.Format.JSON));
            if (descriptor != null) {
                anypointFile = new File(descriptor);
                if (!anypointFile.exists()) {
                    throw new MojoExecutionException("Descriptor not found found: " + descriptor);
                }
            } else {
                anypointFile = ApplicationDescriptor.findAnypointFile(new File("."));
                if (anypointFile == null) {
                    throw new MojoExecutionException("Descriptor not found found: anypoint.json, anypoint.yml or anypoint.yaml");
                }
            }
            objectMapper = new ObjectMapper();
            JsonNode json = objectMapper.readTree(anypointFile);
            JsonNode properties = json.get("properties");
            if (properties != null) {
                for (JsonNode property : properties) {
                    if (isProcessingRequired(property)) {
                        final ObjectNode values = (ObjectNode) property.get("values");
                        if (values != null) {
                            processAndUpdate(key, values, "local");
                            processSubtype(key, values, "envType");
                            processSubtype(key, values, "env");
                        }
                    }
                }
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(anypointFile, json);
        } catch (Exception e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    private void processSubtype(AESKey key, ObjectNode values, String subType) throws Exception {
        final JsonNode envType = values.get(subType);
        if (envType != null) {
            final Iterator<String> it = envType.fieldNames();
            while (it.hasNext()) {
                String fieldname = it.next();
                processAndUpdate(key, (ObjectNode) envType, fieldname);
            }
        }
    }

    private void processAndUpdate(AESKey key, ObjectNode values, String fieldName) throws Exception {
        final JsonNode local = processValueIfNotNull(key, values.get(fieldName));
        if (local != null) {
            values.set(fieldName, local);
        }
    }

    private JsonNode processValueIfNotNull(AESKey key, JsonNode value) throws Exception {
        if (value != null) {
            return processValue(key, value);
        } else {
            return null;
        }
    }

    protected abstract JsonNode processValue(AESKey key, JsonNode value) throws Exception;

    protected abstract boolean isProcessingRequired(JsonNode property);
}
