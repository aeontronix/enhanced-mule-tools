/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.client;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.UUIDFactory;
import com.aeontronix.commons.UnexpectedException;
import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.oidc.OIDCApi;
import com.aeontronix.enhancedmule.oidc.OIDCToken;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jetbrains.annotations.NotNull;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;

import static com.aeontronix.enhancedmule.tools.client.EMTLoginHelper.renderPage;
import static com.aeontronix.kryptotek.DigestUtils.sha256;
import static java.nio.charset.StandardCharsets.US_ASCII;

public class EMTClient {
    private final OIDCApi openIdConnectAPI;
    private final ConfigProfile configProfile;

    public EMTClient(@NotNull ConfigProfile configProfile) {
        ResteasyClient client = (ResteasyClient) ClientBuilder.newBuilder()
                .build();
        this.configProfile = configProfile;
        final ResteasyWebTarget target = client.target(this.configProfile.getServerUrl());
        openIdConnectAPI = target.proxy(OIDCApi.class);
    }

    @SuppressWarnings("unchecked")
    public OIDCToken login() throws IOException {
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            String verifier = UUIDFactory.generate().toString() + UUIDFactory.generate();
            final String challenge = StringUtils.base64Encode(sha256(verifier.getBytes(US_ASCII)), true);
            final String redirectUrl = "http://localhost:" + serverSocket.getLocalPort() + "/";
            final String authorizeUrl = authorize(challenge, redirectUrl);
            System.out.println("Initiate authentication: " + authorizeUrl);
            Desktop.getDesktop().browse(URI.create(authorizeUrl));
            try (final Socket socket = serverSocket.accept(); BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                final String line = r.readLine();
                final String code = EMTLoginHelper.getCode(line);
                final OIDCToken tokens = openIdConnectAPI.token("authorization_code", verifier, code, redirectUrl);
                renderPage(socket, "Login successful, you can close this browser window");
                return tokens;
            }
        }
    }

    private String authorize(String codeChallenge, String redirectUrl) {
        final Response response = openIdConnectAPI.authorize(null, "code", redirectUrl, null, codeChallenge, "S256");
        if (response.getStatus() != 302) {
            throw new UnexpectedException("Authorization returned status code " + response.getStatus() + " rather than 302");
        }
        final String location = response.getHeaderString("Location");
        if (StringUtils.isBlank(location)) {
            throw new UnexpectedException("Authorization Location header missing");
        }
        return location;
    }
}
