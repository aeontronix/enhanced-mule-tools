/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.utils;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.net.URI;

import static java.io.File.separator;

public class ConfigHelper {
    public static void updateConfig(EMConfig config, ConfigProfile configProfile,
                                    URI serverUrl, File mvnSettingsFile, String mavenSettingsId) throws XPathExpressionException, IOException, SAXException {
        if( mvnSettingsFile == null ) {
            mvnSettingsFile = new File(System.getProperty("user.home") + separator + ".m2" + separator + "settings.xml");
        }
        if (serverUrl != null) {
            configProfile.setServerUrl(serverUrl);
        }
        if (StringUtils.isBlank(mavenSettingsId)) {
            configProfile.setMavenSettingsId(mavenSettingsId);
        }
        config.save();
        final MavenSettingsUpdater mavenSettingsUpdater = new MavenSettingsUpdater(mvnSettingsFile);
        mavenSettingsUpdater.addPluginGroup();
        mavenSettingsUpdater.saveSettings();
    }
}
