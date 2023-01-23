/*
 * Copyright (c) 2023. Aeontronix Inc
 */

package com.aeontronix.enhancedmule.tools.config;

public class ConfigProfile {
    private EMCredentials emCredentials;
    private ConfigCredentials credentials;
    private boolean insecureServer;
    private String serverUrl;
    private String anypointUrl;
    private String orgIdLock;
    private String cryptoKey;
    private String defaultOrg;
    private String defaultEnv;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public EMCredentials getEmCredentials() {
        return emCredentials;
    }

    public void setEmCredentials(EMCredentials emCredentials) {
        this.emCredentials = emCredentials;
    }

    public ConfigCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(ConfigCredentials credentials) {
        this.credentials = credentials;
    }

    public String getCryptoKey() {
        return cryptoKey;
    }

    public void setCryptoKey(String cryptoKey) {
        this.cryptoKey = cryptoKey;
    }

    public String getDefaultOrg() {
        return defaultOrg;
    }

    public void setDefaultOrg(String defaultOrg) {
        this.defaultOrg = defaultOrg;
    }

    public String getDefaultEnv() {
        return defaultEnv;
    }

    public void setDefaultEnv(String defaultEnv) {
        this.defaultEnv = defaultEnv;
    }

    public String getAnypointUrl() {
        return anypointUrl;
    }

    public void setAnypointUrl(String anypointUrl) {
        this.anypointUrl = anypointUrl;
    }

    public String getOrgIdLock() {
        return orgIdLock;
    }

    public void setOrgIdLock(String orgIdLock) {
        this.orgIdLock = orgIdLock;
    }

    public boolean isInsecureServer() {
        return insecureServer;
    }

    public void setInsecureServer(boolean insecureServer) {
        this.insecureServer = insecureServer;
    }
}
