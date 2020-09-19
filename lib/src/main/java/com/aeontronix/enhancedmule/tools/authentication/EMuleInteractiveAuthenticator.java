/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.authentication;

import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kloudtek.kryptotek.CryptoUtils;
import com.kloudtek.kryptotek.DecryptionException;
import com.kloudtek.kryptotek.SymmetricAlgorithm;
import com.kloudtek.kryptotek.key.RSAKeyPair;
import com.kloudtek.kryptotek.key.RSAPrivateKey;
import com.kloudtek.util.StringUtils;
import com.kloudtek.util.UUIDFactory;
import com.kloudtek.util.UnexpectedException;
import org.slf4j.Logger;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.slf4j.LoggerFactory.getLogger;

public class EMuleInteractiveAuthenticator {
    public static final Pattern REQ_MATCHER = Pattern.compile("GET.*?\\?tokens=(.*)\\s*?HTTP/1\\.1");
    private static final Logger logger = getLogger(EMuleInteractiveAuthenticator.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private RESTClient restClient;

    public EMuleInteractiveAuthenticator(RESTClient restClient) {
        this.restClient = restClient;
    }

    public static void main(String[] args) throws Exception {
        final RESTClient restClient = new RESTClient();
        restClient.setBaseUrl("http://localhost:8080/api");
        new EMuleInteractiveAuthenticator(restClient).authenticate();
    }

    @SuppressWarnings("unchecked")
    public AccessTokens authenticate() throws HttpException {
        try {
            final RSAKeyPair keyPair = CryptoUtils.generateRSAKeyPair(2048);
            HashMap<String, String> req = new HashMap<>();
            ServerSocket serverSocket = new ServerSocket(0);
            req.put("requestId", UUIDFactory.generate().toString());
            req.put("url", "http://localhost:" + serverSocket.getLocalPort());
            req.put("key", StringUtils.base64Encode(keyPair.getPublicKey().getEncoded().getEncodedKey()));
            String redirectUrl = restClient.postJson("/public/anypoint/token", req).execute(String.class);
            logger.info("Interactive Single Sign On Login - Please complete authentication using browser");
            Desktop.getDesktop().browse(URI.create(redirectUrl));
            return handleCallback(serverSocket, keyPair.getPrivateKey());
        } catch (IOException e) {
            throw new UnexpectedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public AccessTokens handleCallback(ServerSocket serverSocket, RSAPrivateKey privateKey) throws IOException {
        try (final Socket connection = serverSocket.accept();
             BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
             PrintStream pout = new PrintStream(new BufferedOutputStream(connection.getOutputStream()))
        ) {
            String request = in.readLine();
            while (true) {
                String ignore = in.readLine();
                if (ignore == null || ignore.length() == 0) break;
            }
            final Matcher matcher = REQ_MATCHER.matcher(request);
            if (!matcher.find()) {
                pout.print("HTTP/1.0 400 Bad Request");
            } else {
                AccessTokens tokens;
                String encryptedTokenStr = matcher.group(1);
                try {
                    final byte[] decrypt = CryptoUtils.decrypt(privateKey, SymmetricAlgorithm.AES, 256, StringUtils.base64Decode(encryptedTokenStr, true));
                    tokens = objectMapper.readValue(decrypt, AccessTokens.class);
                } catch (DecryptionException e) {
                    sendResponse(pout, "400 Bad Request", null);
                    return null;
                }
                String response = "<html><body><center><h1>Authentication Successful</h1><h3>You can now close this window</h3></center></body></html>";
                sendResponse(pout, "200 OK", response);
                return tokens;
            }
        }
        return null;
    }

    private void sendResponse(PrintStream pout, String status, String response) {
        pout.print("HTTP/1.0 " + status + "\n" + "Content-Type: \n" + "Date: " + new Date() + "\n");
        if (response != null) {
            pout.print("Content-length: " + response.length() + "\n\n" + response);
        }
    }
}