/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools;

import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.util.restclient.RESTException;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.component.annotations.Component;
import org.modelmapper.ModelMapper;

import java.io.File;
import java.io.IOException;

import static org.apache.maven.wagon.events.TransferEvent.REQUEST_GET;
import static org.apache.maven.wagon.events.TransferEvent.TRANSFER_PROGRESS;

@Component(role = Wagon.class, hint = "exchange", instantiationStrategy = "per-lookup")
public class ExchangeWagon extends org.apache.maven.wagon.AbstractWagon {
    public EnhancedMuleClient enhancedMuleClient;
    private ModelMapper modelMapper;

    public ExchangeWagon() {
        modelMapper = new ModelMapper();
        enhancedMuleClient = EMTExtension.emClient;
    }

    @Override
    protected void openConnectionInternal() throws ConnectionException, AuthenticationException {

    }

    @Override
    protected void closeConnection() throws ConnectionException {

    }

    @SuppressWarnings("Convert2Lambda")
    @Override
    public void get(String source, File dest) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        try {
            Resource resource = new Resource(source);
            // Note: Using a lamdba here for some weird reason causes build to break ?!!?
            enhancedMuleClient.getExchangeClient().downloadMavenAsset(source, dest, new IOUtils.DataCopyListener() {
                @Override
                public void dataCopied(byte[] data, int len) throws IOException {
                    final TransferEvent event = new TransferEvent(ExchangeWagon.this,resource, TRANSFER_PROGRESS, REQUEST_GET);
                    fireTransferProgress(event, data, len );
                }
            });
            postProcessListeners(new Resource(source), dest, REQUEST_GET);
        } catch (IOException e) {
            if (e instanceof RESTException) {
                final int statusCode = ((RESTException) e).getStatusCode();
                if (statusCode == 404) {
                    throw new ResourceDoesNotExistException(e.getMessage(), e);
                } else if (statusCode == 401 || statusCode == 403) {
                    throw new AuthorizationException(e.getMessage(), e);
                }
            }
            throw new TransferFailedException(e.getMessage(), e);
        }
    }

    @Override
    public boolean getIfNewer(String source, File dest, long timestamp) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        get(source, dest);
        return true;
    }

    @Override
    public void put(File file, String s) throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
        System.out.println();
    }
}
