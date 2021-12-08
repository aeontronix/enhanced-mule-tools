/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.client;

import com.aeontronix.enhancedmule.tools.client.EMTClient;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import java.io.IOException;

public class ClientAuthenticationRequestFilter implements ClientRequestFilter {
    private EMTClient emtClient;

    public ClientAuthenticationRequestFilter(EMTClient emtClient) {
        this.emtClient = emtClient;
    }

    @Override
    public void filter(ClientRequestContext clientRequestContext) throws IOException {
        final String bearerToken = emtClient.getBearerToken();
        if( bearerToken != null ) {
            clientRequestContext.getHeaders().add("Authorization","Bearer "+bearerToken);
        }
    }
}
