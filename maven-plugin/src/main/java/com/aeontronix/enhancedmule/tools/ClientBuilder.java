/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProvider;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderBearerTokenImpl;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderConnectedAppsImpl;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProviderUsernamePasswordImpl;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Settings;
import org.slf4j.Logger;

import static com.aeontronix.commons.StringUtils.isNotBlank;
import static org.slf4j.LoggerFactory.getLogger;

public class ClientBuilder {
    private static final Logger logger = getLogger(ClientBuilder.class);

    public static AnypointClient buildClient(String bearerToken, String username, String password, String clientId,
                                             String clientSecret, Settings settings) {
        AuthenticationProvider authenticationProvider;
        if (bearerToken != null) {
            authenticationProvider = new AuthenticationProviderBearerTokenImpl(bearerToken);
        } else if (isNotBlank(username) && isNotBlank(password)) {
            logger.debug("Using username/password credentials: {}", username);
            authenticationProvider = new AuthenticationProviderUsernamePasswordImpl(username, password);
        } else if (isNotBlank(clientId) && isNotBlank(clientSecret)) {
            logger.debug("Using client credentials: {}", clientId);
            authenticationProvider = new AuthenticationProviderConnectedAppsImpl(clientId, clientSecret);
        } else {
            throw new IllegalArgumentException("No authentication credentials specified (username/password, client id/secret or bearer)");
        }
        AnypointClient client = new AnypointClient(authenticationProvider);
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
