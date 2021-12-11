/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.client;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.UUIDFactory;
import com.aeontronix.commons.UnexpectedException;
import com.aeontronix.commons.xml.XPathUtils;
import com.aeontronix.commons.xml.XmlUtils;
import com.aeontronix.enhancedmule.config.ConfigProfile;
import com.aeontronix.enhancedmule.oidc.OIDCApi;
import com.aeontronix.enhancedmule.oidc.OIDCToken;
import com.aeontronix.enhancedmule.oidc.UserInfo;
import com.aeontronix.enhancedmule.tools.cli.LoginResult;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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

import static com.aeontronix.commons.xml.XmlUtils.getChildElement;
import static com.aeontronix.kryptotek.DigestUtils.sha256;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.slf4j.LoggerFactory.getLogger;

public class LoginHelper {
    private static final Logger logger = getLogger(LoginHelper.class);
    public static final Pattern codeMatcher = Pattern.compile("code=(.*)[\\s&]");

    public static void login(File mvnSettingsFile, String mavenSettings, EMTClient emtClient, ConfigProfile configProfile) throws IOException, SAXException, XPathExpressionException {
        final LoginResult res = oidcLogin(emtClient);
        final String accessToken = res.getToken().getAccessToken();
        configProfile.setBearerToken(accessToken);
        configProfile.setRefreshToken(res.getToken().getRefreshToken());
        updateMavenSettings(mvnSettingsFile, mavenSettings, res);
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

    private static void updateMavenSettings(File mvnSettingsFile, String mavenSettings, LoginResult res) throws IOException, SAXException, XPathExpressionException {
        if (StringUtils.isBlank(mavenSettings)) {
            mavenSettings = res.getUserInfo().getMavenSettings();
        }
        if (mavenSettings != null) {
            logger.info("Updating maven settings using id " + mavenSettings);
            final Document settingsDoc;
            final Element settings;
            if (mvnSettingsFile.exists()) {
                settingsDoc = XmlUtils.parse(mvnSettingsFile, true);
                settings = settingsDoc.getDocumentElement();
                logger.debug("Loaded " + mvnSettingsFile.getPath());
            } else {
                settingsDoc = XmlUtils.createDocument(true);
                settings = settingsDoc.createElementNS("http://maven.apache.org/SETTINGS/1.0.0", "settings");
                settingsDoc.appendChild(settings);
                settings.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
                settings.setAttribute("xsi:schemaLocation", "http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd");
                logger.debug("Created " + mvnSettingsFile.getPath());
            }
            // Add profile and property
            final Element profiles = getChildElement(settings, "profiles", true);
            Element profile = XPathUtils.evalXPathElement("*[local-name()='profile']/*[local-name()='id'][text() = '" + mavenSettings + "']/..", profiles);
            if (profile == null) {
                profile = XmlUtils.createElement("profile", profiles);
                XmlUtils.createElement("activeByDefault", XmlUtils.createElement("activation", profile))
                        .setTextContent("true");
                XmlUtils.createElement("id", profile).setTextContent(mavenSettings);
            }
            final Element properties = getChildElement(profile, "properties", true);
            getChildElement(properties, mavenSettings, true).setTextContent(res.getUserInfo().getAnypointBearerToken());
            // add servers
            final Element servers = getChildElement(settings, "servers", true);
            Element server = XPathUtils.evalXPathElement("*[local-name()='server']/*[local-name()='id'][text() = '" + mavenSettings + "']/..", servers);
            if (server == null) {
                server = XmlUtils.createElement("server", servers);
                XmlUtils.createElement("id", server).setTextContent(mavenSettings);
            }
            getChildElement(server, "username", true).setTextContent("~~~Token~~~");
            getChildElement(server, "password", true).setTextContent(res.getUserInfo().getAnypointBearerToken());
            // Add maven plugin group
            if( XPathUtils.evalXPathElement("*[local-name()='pluginGroups']/*[local-name()='pluginGroup'][text() = 'com.aeontronix.enhanced-mule']", settings) == null ) {
                getChildElement(getChildElement(settings, "pluginGroups", true),"pluginGroup",true)
                        .setTextContent("com.aeontronix.enhanced-mule");
            }
            // write settings
            try (final FileWriter w = new FileWriter(mvnSettingsFile)) {
                XmlUtils.serialize(settingsDoc, w, true, true);
            }
        }
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
