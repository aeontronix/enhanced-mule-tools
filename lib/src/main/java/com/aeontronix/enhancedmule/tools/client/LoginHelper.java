/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.client;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.UUIDFactory;
import com.aeontronix.commons.UnexpectedException;
import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.config.EMConfig;
import com.aeontronix.enhancedmule.oidc.OIDCApi;
import com.aeontronix.enhancedmule.oidc.OIDCToken;
import com.aeontronix.enhancedmule.oidc.UserInfo;
import com.aeontronix.enhancedmule.tools.cli.LoginResult;
import com.aeontronix.enhancedmule.tools.utils.MavenSettingsUpdater;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Response;
import javax.xml.xpath.XPathExpressionException;
import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aeontronix.kryptotek.DigestUtils.sha256;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.slf4j.LoggerFactory.getLogger;

public class LoginHelper {
    private static final Logger logger = getLogger(LoginHelper.class);
    public static final Pattern codeMatcher = Pattern.compile("code=(.*)[\\s&]");

    public static void login(File mvnSettingsFile, String mavenSettingsId, EMTClient emtClient, EMConfig configFile,
                             ConfigProfile configProfile, URI serverUrl) throws IOException, SAXException, XPathExpressionException {
        if (serverUrl != null) {
            configProfile.setServerUrl(serverUrl);
        }
        final LoginResult res = oidcLogin(emtClient);
        final String accessToken = res.getToken().getAccessToken();
        configProfile.setBearerToken(accessToken);
        configProfile.setRefreshToken(res.getToken().getRefreshToken());
        if (StringUtils.isBlank(mavenSettingsId)) {
            mavenSettingsId = res.getUserInfo().getMavenSettingsId();
        }
        final MavenSettingsUpdater mavenSettingsUpdater = new MavenSettingsUpdater(mvnSettingsFile);
        mavenSettingsUpdater.addBearerToken(mavenSettingsId, accessToken);
        mavenSettingsUpdater.addPluginGroup();
        mavenSettingsUpdater.saveSettings();
        configFile.save();
    }

    @NotNull
    public static LoginResult oidcLogin(EMTClient client) throws IOException {
        LoginResult result;
        OIDCApi openIdConnectAPI = client.getOpenIdConnectAPI();
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            String verifier = UUIDFactory.generate().toString() + UUIDFactory.generate();
            final String challenge = StringUtils.base64Encode(sha256(verifier.getBytes(US_ASCII)), true);
            final String redirectUrl = "http://localhost:" + serverSocket.getLocalPort() + "/";
            final String authorizeUrl = authorize(openIdConnectAPI, challenge, redirectUrl);
            logger.info("Initiate authentication: " + authorizeUrl);
            Desktop.getDesktop().browse(URI.create(authorizeUrl));
            try (final Socket socket = serverSocket.accept(); BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                final String line = r.readLine();
                final String code = getCode(line);
                final OIDCToken tokens = openIdConnectAPI.token("authorization_code", verifier, code, redirectUrl);
                client.updateBearerToken(tokens.getAccessToken());
                renderPage(socket, "Login successful, you can close this browser window");
                final UserInfo userInfo = openIdConnectAPI.getUserInfo();
                result = new LoginResult(tokens, userInfo);
            }
        }
        return result;
    }

    public static String authorize(OIDCApi openIdConnectAPI, String codeChallenge, String redirectUrl) {
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

    public static void renderPage(Socket socket, String message) throws IOException {
        try (final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            w.append("HTTP/1.1 200 Ok\nContent-Type: text/html\n\n");
            w.append("<html><body><center>");
            w.append(message);
            w.append("</center></body></html>");
        }
    }

    public static String getCode(String line) throws IOException {
        final Matcher matcher = codeMatcher.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IOException("Invalid response: " + line);
        }
    }

}
