/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.util;

import org.slf4j.Logger;
import picocli.CommandLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.slf4j.LoggerFactory.getLogger;

public class VersionHelper implements CommandLine.IVersionProvider {
    private static final Logger logger = getLogger(VersionHelper.class);
    public static final String EMT_VERSION = getPOMVersion();
    public static final String[] EMT_VERSION_ARR = EMT_VERSION != null ? new String[]{EMT_VERSION} : new String[]{"SNAPSHOT"};

    @Override
    public String[] getVersion() throws Exception {
        return EMT_VERSION_ARR;
    }

    private static String getPOMVersion() {
        try {
            final InputStream pomProperties = VersionHelper.class.getResourceAsStream("/META-INF/maven/com.aeontronix.enhanced-mule/enhanced-mule-tools-lib/pom.properties");
            if (pomProperties != null) {
                Properties p = new Properties();
                p.load(pomProperties);
                return p.get("version").toString();
            }
        } catch (IOException e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }
}
