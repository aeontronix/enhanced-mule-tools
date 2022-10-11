/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.UUIDFactory;
import com.aeontronix.enhancedmule.config.CredentialsBearerTokenImpl;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.RESTClientHost;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aeontronix.kryptotek.DigestUtils.sha256;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static org.slf4j.LoggerFactory.getLogger;

@CommandLine.Command(name = "login", description = "Login to server")
public class LoginCmd extends AbstractCommand implements Callable<Integer> {
    private static final Logger logger = getLogger(LoginCmd.class);
    public static final Pattern codeMatcher = Pattern.compile("code=(.*)[\\s&]");

    @SuppressWarnings("unchecked")
    @Override
    public Integer call() throws Exception {
        try (final ServerSocket serverSocket = new ServerSocket(0)) {
            final String state = UUIDFactory.generate().toString();
            String verifier = UUIDFactory.generate().toString() + UUIDFactory.generate();
            final String challenge = StringUtils.base64Encode(sha256(verifier.getBytes(US_ASCII)), true);
            final String redirectUrl = "http://localhost:" + serverSocket.getLocalPort() + "/";
            final EMTCli cli = getCli();
            final RESTClient restClient = cli.getClient().getRestClient();
            final String authServerBaseUrl = "https://auth.enhanced-mule.com/v1/oidc";
            final RESTClientHost authServerClient = restClient.host(authServerBaseUrl).build();
            final URI authorizeUri = new URLBuilder(authServerBaseUrl + "/authorize")
                    .queryParam("redirect_uri", redirectUrl)
                    .queryParam("state", state)
                    .queryParam("code_challenge", challenge)
                    .queryParam("code_challenge_method", "S256")
                    .toUri();
            logger.info("Authenticating request: " + authorizeUri);
            try {
                Desktop.getDesktop().browse(authorizeUri);
            } catch (Exception e) {
                logger.warn("Unable to launch browser, please manually open web browser and navigate to : " + authorizeUri);
            }
            try (final Socket socket = serverSocket.accept(); BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                final String line = r.readLine();
                final String code = getCode(line);
                final Map<String, String> req = new HashMap<>();
                req.put("grant_type", "authorization_code");
                req.put("redirect_uri", redirectUrl);
                req.put("code_verifier", verifier);
                req.put("code", code);
                final Map<String, String> tokens = authServerClient.post("token").jsonBody(req)
                        .executeAndConvertToObject(Map.class);
                final String anAccessToken = tokens.get("an_access_token");
                cli.getActiveProfile().setCredentials(new CredentialsBearerTokenImpl(anAccessToken));
                cli.saveConfig();
                renderPage(socket, "Login successful, you can close this browser window");
            }
        }
        return 0;
    }

    public static String getCode(String line) throws IOException {
        final Matcher matcher = codeMatcher.matcher(line);
        if (matcher.find()) {
            return URLDecoder.decode(matcher.group(1), "UTF-8");
        } else {
            throw new IOException("Invalid response: " + line);
        }
    }


    public static void renderPage(Socket socket, String message) throws IOException {
        try (final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            w.append("HTTP/1.1 200 Ok\nContent-Type: text/html\n\n");
            w.append("<html><body><center>");
            w.append(message);
            w.append("</center></body></html>");
        }
    }
}
