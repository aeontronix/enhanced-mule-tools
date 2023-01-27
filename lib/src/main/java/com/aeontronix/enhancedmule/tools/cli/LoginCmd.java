/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.UUIDFactory;
import com.aeontronix.enhancedmule.tools.config.ConfigProfile;
import com.aeontronix.enhancedmule.tools.config.CredentialsBearerTokenImpl;
import com.aeontronix.enhancedmule.tools.util.MavenHelper;
import com.aeontronix.restclient.RESTClient;
import org.slf4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import static com.aeontronix.kryptotek.DigestUtils.sha256;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.slf4j.LoggerFactory.getLogger;

@Command(name = "login", description = "Login to server")
public class LoginCmd extends AbstractCommand implements Callable<Integer> {
    private static final Logger logger = getLogger(LoginCmd.class);
    public static final Pattern codeMatcher = Pattern.compile("code=(.*?)&");
    @Option(names = {"-ss", "--skip-maven-settings-update"},
            description = "If set to true, maven settings.xml will not be updated with bearer token",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private boolean updateSettingsXml;
    @Option(names = {"-sf", "--maven-settings-file"},
            description = "Maven settings.xml file location",
            showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private File mavenSettingsFile = new File(System.getProperty("user.home") + File.separator + ".m2" + File.separator + "settings.xml");

    @SuppressWarnings("unchecked")
    @Override
    public Integer call() throws Exception {
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            final String state = UUIDFactory.generate().toString();
            String verifier = UUIDFactory.generate().toString() + UUIDFactory.generate();
            final String challenge = StringUtils.base64EncodeToString(sha256(verifier.getBytes(US_ASCII)), true);
            final String redirectUrl = "http://localhost:" + serverSocket.getLocalPort() + "/";
            final EMTCli cli = getCli();
            RESTClient restClient = cli.createEMClient().getRestClient();
            ConfigProfile activeProfile = cli.getActiveProfile();
            String authServerBaseUrl = activeProfile.getServerUrl();
            if (authServerBaseUrl == null) {
                authServerBaseUrl = "https://auth.enhanced-mule.com";
            }
            final Map oidcCfg = restClient.get(new URLBuilder(authServerBaseUrl).path("/.well-known/openid-configuration").toUri()).executeAndConvertToObject(Map.class);
            final String authorizationEndpoint = (String) oidcCfg.get("authorization_endpoint");
            final String tokenEndpoint = (String) oidcCfg.get("token_endpoint");
            URLBuilder authzUrlBuilder = new URLBuilder(authorizationEndpoint)
                    .queryParam("redirect_uri", redirectUrl)
                    .queryParam("state", state)
                    .queryParam("code_challenge", challenge)
                    .queryParam("code_challenge_method", "S256")
                    .queryParam("scope", "full");
            String orgDomain = activeProfile.getOrgDomain();
            if (orgDomain != null) {
                authzUrlBuilder.queryParam("org_domain", orgDomain);
            }
            final URI authorizeUri = authzUrlBuilder.toUri();
            logger.info("Authenticating request: " + authorizeUri);
            try {
                Desktop.getDesktop().browse(authorizeUri);
            } catch (Exception e) {
                logger.warn("Unable to launch browser, please manually open web browser and navigate to : " + authorizeUri);
            }
            try (final Socket socket = serverSocket.accept();
                 BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
                final String line = r.readLine();
                String result;
                Map<String, String> params = new HashMap<>();
                for (String str : URI.create(line.split(" ")[1]).getQuery().split("&")) {
                    String[] entry = str.split("=");
                    params.put(entry[0], StringUtils.urlDecode(entry[1]));
                }
                final String code = params.get("code");
                final String callbackState = params.get("state");
                if (!callbackState.equals(state)) {
                    throw new IllegalArgumentException("Received state does not match sent");
                }
                final Map<String, String> req = new HashMap<>();
                req.put("grant_type", "authorization_code");
                req.put("redirect_uri", redirectUrl);
                req.put("code_verifier", verifier);
                req.put("code", code);
                final Map<String, String> tokens = restClient.post(tokenEndpoint).jsonBody(req)
                        .executeAndConvertToObject(Map.class);
                final String anAccessToken = tokens.get("an_access_token");
                activeProfile.setCredentials(new CredentialsBearerTokenImpl(anAccessToken));
                cli.saveConfig();
                if (!updateSettingsXml) {
                    MavenHelper.updateMavenSettings(mavenSettingsFile, cli.getActiveProfileId(), anAccessToken);
                }
                String redirectTo = params.get("redirectTo");
                if (redirectTo != null) {
                    w.write("HTTP/1.1 302 Found\nContent-Type: text/html\nLocation: " + redirectTo + "\n\n");
                } else {
                    w.append("HTTP/1.1 200 Ok\nContent-Type: text/html\n\n");
                    w.append("<html><body><center>");
                    w.append("Login successful, you can close this browser window");
                    w.append("</center></body></html>");
                }
            }
        }
        return 0;
    }
}
