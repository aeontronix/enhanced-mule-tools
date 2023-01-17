/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import com.aeontronix.enhancedmule.tools.emclient.EnhancedMuleClient;
import com.aeontronix.enhancedmule.tools.emclient.authentication.AnypointBearerTokenCredentialsProvider;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@Component(role = AbstractMavenLifecycleParticipant.class)
public class EMTExtension extends AbstractMavenLifecycleParticipant {
    public static final String PLUGINKEY = "com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin";
    public static final String TOKEN = "~~~Token~~~";
    public static final String ENHANCED_MULE_CLIENT = "enhancedMuleClient";
    private static final Logger logger = getLogger(EMTExtension.class);
    private static ConfigProfile configProfile;
    private EnhancedMuleClient emClient;
    private AnypointBearerTokenCredentialsProvider credentialsProvider;
    private boolean mulePluginCompatibility;
    private String enhancedMuleServerUrl;
    private String anypointPlatformUrl;
    private String anypointBearerToken;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String serverId;
    private String org;
    private String profileName;
    private static EMConfig emConfig;

    @Override
    public void afterProjectsRead(MavenSession session) throws MavenExecutionException {
        logger.warn("Usage of plugin as extension is no longer required, please remove");
    }
}
