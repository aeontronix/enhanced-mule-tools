/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.authentication;

import com.aeontronix.enhancedmule.tools.util.AnypointAccessToken;
import com.aeontronix.enhancedmule.tools.util.EMHttpClient;
import com.aeontronix.enhancedmule.tools.util.EMHttpClientDefaultImpl;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.util.HttpHelper;
import com.aeontronix.commons.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;

public abstract class AuthenticationProvider {
    public abstract AnypointAccessToken getBearerToken(HttpHelper httpHelper) throws HttpException;

    public EMHttpClient createHttpClient() {
        return createHttpClient(null,null,null);
    }

    public EMHttpClient createHttpClient(HttpHost proxyHost, String proxyUsername, String proxyPassword ) {
        HttpClientBuilder builder = HttpClients.custom().disableCookieManagement();
        if( proxyHost != null ) {
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
            builder = builder.setRoutePlanner(routePlanner);
            if (StringUtils.isNotEmpty(proxyUsername) && StringUtils.isNotEmpty(proxyPassword)) {
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(new AuthScope(proxyHost), new UsernamePasswordCredentials(proxyUsername, proxyPassword));
                builder = builder.setDefaultCredentialsProvider(credsProvider);
            }
        }
        return new EMHttpClientDefaultImpl(builder.build());
    }

    public abstract String filterSecret(String resStr);
}
