/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.config;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.io.IOUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.util.Map;

public class ConfigFile extends ConfigProfile {
    @JsonProperty("default")
    private String defaultProfile;
    @JsonProperty("profiles")
    private Map<String, ConfigProfile> profiles;

    public static ConfigProfile findConfigProfile(String org) throws IOException {
        final ConfigFile configFile = findConfigFile();
        if (configFile != null) {
            return configFile.getMatchingProfile(org);
        }
        return null;
    }

    public static ConfigFile findConfigFile() throws IOException {
        return findConfigFile(null);
    }

    public static ConfigFile findConfigFile(String filename) throws IOException {
        if (filename == null) {
            filename = "enhanced-mule.config.json";
        }
        InputStream is = null;
        try {
            is = findConfig(filename);
            if (is != null) {
                return new ObjectMapper().readValue(is, ConfigFile.class);
            }
        } finally {
            IOUtils.close(is);
        }
        return null;
    }

    private static InputStream findConfig(String filename) throws FileNotFoundException {
        File file = new File(filename);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        final String userHome = System.getProperty("user.home");
        file = new File(userHome + File.separatorChar + "." + filename);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        file = new File(userHome + File.separatorChar + ".enhanced-mule" + File.separatorChar + filename);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        InputStream is = ConfigFile.class.getClassLoader().getResourceAsStream(filename);
        if (is != null) {
            return is;
        }
        is = ConfigFile.class.getClassLoader().getResourceAsStream("/" + filename);
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

    public ConfigProfile getMatchingProfile(String org) {
        if (profiles == null) {
            return this;
        } else {
            if (StringUtils.isNotBlank(org)) {
                for (ConfigProfile p : profiles.values()) {
                    if (p.getOrgs() != null) {
                        for (String profileOrg : p.getOrgs()) {
                            if (org.equalsIgnoreCase(profileOrg)) {
                                return p;
                            }
                        }
                    }
                }
            }
            if (defaultProfile != null) {
                return profiles.get(defaultProfile);
            }
        }
        return null;
    }

    public String getDefaultProfile() {
        return defaultProfile;
    }

    public void setDefaultProfile(String defaultProfile) {
        this.defaultProfile = defaultProfile;
    }

    public Map<String, ConfigProfile> getProfiles() {
        return profiles;
    }

    public void setProfiles(Map<String, ConfigProfile> profiles) {
        this.profiles = profiles;
    }
}
