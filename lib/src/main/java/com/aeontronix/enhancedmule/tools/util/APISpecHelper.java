/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;

import static com.aeontronix.enhancedmule.tools.util.JsonHelper.isNotNull;
import static com.aeontronix.enhancedmule.tools.util.JsonHelper.isNull;

public class APISpecHelper {
    @Nullable
    public static APISpecVersion findVersion(@NotNull File file) throws IOException {
        String version = null;
        if (file.exists()) {
            ObjectMapper om;
            if (file.getName().toLowerCase().endsWith("json")) {
                om = new ObjectMapper();
            } else {
                om = new YAMLMapper();
            }
            final JsonNode specNode = om.readTree(file);
            JsonNode specVersionNode = specNode.get("version");
            if (isNull(specVersionNode)) {
                final JsonNode info = specNode.get("info");
                if (isNotNull(info)) {
                    specVersionNode = info.get("version");
                }
            }
            if (specVersionNode != null) {
                version = specVersionNode.textValue();
            }
        }
        if (version == null) {
            return null;
        } else {
            return new APISpecVersion(version);
        }
    }

    public static class APISpecVersion {
        private String version;
        private String nonSnapshotVersion;

        public APISpecVersion(String version) {
            this.version = version;
            final int snapshotIdx = this.version.toLowerCase().indexOf("-snapshot");
            if (snapshotIdx != -1) {
                nonSnapshotVersion = version.substring(0, snapshotIdx);
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
