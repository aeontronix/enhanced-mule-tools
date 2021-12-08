/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.URLBuilder;
import com.aeontronix.commons.UUIDFactory;
import com.aeontronix.commons.UnexpectedException;
import com.fasterxml.jackson.databind.ObjectMapper;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ParentCommand;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.aeontronix.commons.StringUtils.base64Encode;
import static com.aeontronix.kryptotek.DigestUtils.sha256;
import static java.nio.charset.StandardCharsets.US_ASCII;

@Command(name = "login", description = "Authenticate with server")
public class LoginCmd implements Callable<Integer> {
    @Parameters
    private URL server;
    @ParentCommand
    private EMTCli cli;

    @Override
    public Integer call() throws Exception {
        final Map<String, String> tokens = login();
        if (tokens != null) {
            final String accessToken = tokens.get("access_token");
            if (StringUtils.isBlank(accessToken)) {
                throw new UnexpectedException("Access token missing from response");
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> login() throws IOException, URISyntaxException {
        final ServerSocket serverSocket = new ServerSocket(0);
        String verifier = UUIDFactory.generate().toString() + UUIDFactory.generate();
        final String challenge = base64Encode(sha256(verifier.getBytes(US_ASCII)), true);
        int port = serverSocket.getLocalPort();
        final String redirectUri = "http://localhost:" + port;
        final URI uri = new URLBuilder(server).path("api/oidc/authorize")
                .param("redirect_uri", redirectUri)
                .param("code_challenge", challenge).toUri();
        System.out.println("Initiate authentication: " + uri);
        Desktop.getDesktop().browse(uri);
        final Socket socket = serverSocket.accept();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            final String line = r.readLine();
            final String code = getCode(line);
            final HashMap<String, String> tokenRequest = new HashMap<>();
            tokenRequest.put("grant_type", "authorization_code");
            tokenRequest.put("code_verifier", verifier);
            tokenRequest.put("code", code);
            tokenRequest.put("redirect_uri", redirectUri);
            final URL tokenUrl = new URLBuilder(server)
                    .path("api/oidc/token").toUrl();
            HttpURLConnection connection = (HttpURLConnection) tokenUrl.openConnection();
            try {
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                final String formParams = getFormParams(tokenRequest);
                connection.setRequestProperty("Content-Length",
                        Integer.toString(formParams.getBytes().length));
                connection.setRequestProperty("Content-Language", "en-US");
                connection.setUseCaches(false);
                connection.setDoInput(true);
                connection.setDoOutput(true);
                try (DataOutputStream wr = new DataOutputStream(
                        connection.getOutputStream())) {
                    wr.writeBytes(formParams);
                    wr.flush();
                }
                Map<String, String> tokens = null;
                try {
                    try (InputStream inputStream = connection.getInputStream()) {
                        tokens = new ObjectMapper().readValue(inputStream, Map.class);
                    }
                } catch (IOException e) {
                    final int responseCode = connection.getResponseCode();
                    if (responseCode >= 400 && responseCode < 500) {
                        renderPage(socket, "Error, server didn't accept request string sent");
                    } else if (responseCode >= 500) {
                        renderPage(socket, "Server error");
                    }
                }
                renderPage(socket, "Login successful, you can close this browser window");
                return tokens;
            } finally {
                connection.disconnect();
            }
        }
    }

    private void renderPage(Socket socket, String message) throws IOException {
        try (final BufferedWriter w = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {
            w.append("HTTP/1.1 200 Ok\nContent-Type: text/html\n\n");
            w.append("<html><body><center>");
            w.append(message);
            w.append("</center></body></html>");
        }
    }

    private String getCode(String line) throws IOException {
        final Matcher matcher = Pattern.compile("code=(.*)[\\s&]").matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            throw new IOException("Invalid response: " + line);
        }
    }

    private String getFormParams(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");
            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }
        return result.toString();
    }
}
