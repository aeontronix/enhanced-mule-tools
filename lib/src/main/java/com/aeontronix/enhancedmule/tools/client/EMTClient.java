/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.client;

import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.oidc.OIDCApi;
import com.aeontronix.enhancedmule.oidc.OIDCToken;
import com.aeontronix.enhancedmule.oidc.UserInfo;
import com.aeontronix.enhancedmule.tools.cli.LoginRequired;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.net.URI;

import static com.aeontronix.kryptotek.DigestUtils.sha256;

public class EMTClient {
    private final OIDCApi openIdConnectAPI;
    private final ConfigProfile configProfile;

    public EMTClient(@NotNull ConfigProfile configProfile) {
        ResteasyClient client = (ResteasyClient) ClientBuilder.newBuilder()
                .register(new ClientAuthenticationRequestFilter(this))
                .build();
        this.configProfile = configProfile;
        final URI serverUrl = this.configProfile.getServerUrl();
        if( serverUrl != null ) {
            final ResteasyWebTarget target = client.target(serverUrl);
            openIdConnectAPI = target.proxy(OIDCApi.class);
        } else {
            throw new LoginRequired();
        }
    }

    @SuppressWarnings("unchecked")
    public OIDCToken login() throws IOException {
        return EMTLoginHelper.login(openIdConnectAPI);
    }

    public EMTClientStatus getStatus() {
        final EMTClientStatus status = new EMTClientStatus();
        try {
            final UserInfo userInfo = openIdConnectAPI.getUserInfo();
            status.setAuthenticated(true);
            status.setUsername(userInfo.getUsername());
        } catch (NotAuthorizedException e) {
            //
        }
        return status;
    }

    public String getBearerToken() {
        return configProfile.getBearerToken();
    }
}
