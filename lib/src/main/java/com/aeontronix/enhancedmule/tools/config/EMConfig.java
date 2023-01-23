/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class EMConfig {
    public static final String DEFAULT = "default";
    @JsonProperty("profiles")
    private Map<String, ConfigProfile> profiles = new HashMap<>();
    @JsonProperty("active")
    private String active = DEFAULT;

    public EMConfig() {
    }

    public void save() throws IOException {
        final File file = new File(System.getProperty("user.home") + File.separator + ".enhanced-mule.config.json");
        new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .writerWithDefaultPrettyPrinter().writeValue(file, this);
    }

    public static EMConfig findConfigFile() throws IOException {
        return findConfigFile(null);
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public void checkProfileExists(String profileName) throws ProfileNotFoundException {
        if (profileName == null) {
            return;
        }
        if (profiles == null) {
            if (profileName.equalsIgnoreCase(DEFAULT)) {
                return;
            }
        } else if (profiles.containsKey(profileName)) {
            return;
        }
        throw new ProfileNotFoundException(profileName);
    }

    public static EMConfig findConfigFile(String filename) throws IOException {
        if (filename == null) {
            filename = "enhanced-mule.config.json";
        }
        InputStream is = null;
        try {
            is = findConfig(filename);
            if (is != null) {
                return new ObjectMapper().readValue(is, EMConfig.class);
            } else {
                final EMConfig cfg = new EMConfig();
                cfg.getOrCreateProfile(cfg.getActive());
                return cfg;
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                //
            }
        }
    }

    private static InputStream findConfig(String filename) throws FileNotFoundException {
        File file = new File(filename);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        file = new File(System.getProperty("user.home") + File.separatorChar + "." + filename);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        file = new File(System.getProperty("user.home") + File.separatorChar + ".enhanced-mule" + File.separatorChar + filename);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        InputStream is = EMConfig.class.getClassLoader().getResourceAsStream(filename);
        if (is != null) {
            return is;
        }
        is = EMConfig.class.getClassLoader().getResourceAsStream("/" + filename);
        if (is != null) {
            return is;
        }
        is = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
        if (is != null) {
            return is;
        }
        is = Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + filename);
        return is;
    }

    @JsonIgnore
    public ConfigProfile getOrCreateProfile(String profile) {
        ConfigProfile configProfile = profiles.get(profile);
        if (configProfile == null) {
            configProfile = new ConfigProfile();
        }
        return configProfile;
    }

    @JsonIgnore
    public ConfigProfile getActiveProfile() {
        return getProfiles().computeIfAbsent(active, s -> new ConfigProfile());
    }

    @JsonIgnore
    public ConfigProfile getProfile(@Nullable String profile) throws ProfileNotFoundException {
        return profile != null ? getProfileByProfileName(profile) : getActiveProfile();
    }

    @JsonIgnore
    public ConfigProfile getProfileByProfileName(@NotNull String profile) throws ProfileNotFoundException {
        ConfigProfile p = profiles.get(profile);
        if (p != null) {
            return p;
        } else if (DEFAULT.equalsIgnoreCase(profile)) {
            p = new ConfigProfile();
            profiles.put(DEFAULT, p);
            return p;
        } else {
            throw new ProfileNotFoundException(profile);
        }
    }

    @JsonIgnore
    private ConfigProfile getDefaultProfile() {
        return profiles.computeIfAbsent(DEFAULT, s -> new ConfigProfile());
    }

    @NotNull
    public Map<String, ConfigProfile> getProfiles() {
        if (profiles == null) {
            profiles = new HashMap<>();
            profiles.put(DEFAULT, new ConfigProfile());
        }
        return profiles;
    }

    public void setProfiles(@NotNull Map<String, ConfigProfile> profiles) {
        this.profiles = profiles;
    }

    private static boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
