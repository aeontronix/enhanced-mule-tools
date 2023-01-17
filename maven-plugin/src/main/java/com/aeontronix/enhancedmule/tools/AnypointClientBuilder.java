/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProvider;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderBearerTokenImpl;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class AnypointClientBuilder {
    private static final Logger logger = getLogger(AnypointClientBuilder.class);

    public static LegacyAnypointClient buildClient(String bearerToken,
                                                   Settings settings, String anypointBaseUrl) {
        AuthenticationProvider authenticationProvider;
        if (bearerToken != null) {
            authenticationProvider = new AuthenticationProviderBearerTokenImpl(bearerToken);
        } else {
            throw new IllegalArgumentException("No authentication credentials specified (username/password, client id/secret or bearer)");
        }
        LegacyAnypointClient client = new LegacyAnypointClient(authenticationProvider, anypointBaseUrl);
        Proxy proxy = settings.getActiveProxy();
        logger.debug("Checking debug settings");
        if (proxy != null) {
            logger.debug("Using proxy: " + proxy.getProtocol() + " " + proxy.getHost() + " " + proxy.getPort());
            client.setProxy(proxy.getProtocol(), proxy.getHost(), proxy.getPort(), proxy.getUsername(), proxy.getPassword());
        } else {
            logger.debug("No proxy specified");
        }
        return client;
    }
}
