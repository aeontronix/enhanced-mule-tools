/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import java.io.File;
import java.io.IOException;

public class APISpecHelper {
    public static APISpecVersion findVersion(File file) throws IOException {
        String version = null;
        if (file.exists()) {
            ObjectMapper om;
            if (file.getName().toLowerCase().endsWith("json")) {
                om = new ObjectMapper();
            } else {
                om = new YAMLMapper();
            }
            final JsonNode specNode = om.readTree(file);
            final JsonNode specVersionNode = specNode.get("version");
            if (specVersionNode != null) {
                version = specVersionNode.textValue();
            }
        }
        if( version == null ) {
            version = "1.0.0";
        }
        return new APISpecVersion(version);
    }

    public static class APISpecVersion {
        private String version;
        private String nonSnapshotVersion;

        public APISpecVersion(String version) {
            this.version = version;
            final int snapshotIdx = this.version.toLowerCase().indexOf("-snapshot");
            if(snapshotIdx != -1) {
                nonSnapshotVersion = version.substring(0,snapshotIdx);
            } else {
                nonSnapshotVersion = version;
            }
        }

        public String getVersion() {
            return version;
        }

        public String getNonSnapshotVersion() {
            return nonSnapshotVersion;
        }
    }
}
