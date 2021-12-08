/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.client;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EMTLoginHelper {
    public static final Pattern codeMatcher = Pattern.compile("code=(.*)[\\s&]");

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

    public static String getFormParams(HashMap<String, String> params) throws UnsupportedEncodingException {
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
