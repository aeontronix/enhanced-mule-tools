/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.util.restclient;

import com.aeontronix.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class ResponseStream extends InputStream {
    private final InputStream content;
    private final long contentLength;
    private CloseableHttpResponse response;
    private long lastModified = 0;

    public ResponseStream(CloseableHttpResponse response) throws IOException {
        this.response = response;
        final HttpEntity entity = response.getEntity();
        final Header lastModifiedHeader = response.getLastHeader("last-modified");
        if( lastModifiedHeader != null && lastModifiedHeader.getValue() != null ) {
            lastModified = Long.parseLong(lastModifiedHeader.getValue());
        }
        contentLength = response.getEntity().getContentLength();
        content = entity.getContent();
    }

    public long getContentLength() {
        return contentLength;
    }

    public long getLastModified() {
        return lastModified;
    }

    @Override
    public int read() throws IOException {
        return content.read();
    }

    @Override
    public int read(@NotNull byte[] b) throws IOException {
        return content.read(b);
    }

    @Override
    public int read(@NotNull byte[] b, int off, int len) throws IOException {
        return content.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return content.skip(n);
    }

    @Override
    public int available() throws IOException {
        return content.available();
    }

    @Override
    public void close() throws IOException {
        IOUtils.close(content, response);
    }

    @Override
    public void mark(int readlimit) {
        content.mark(readlimit);
    }

    @Override
    public void reset() throws IOException {
        content.reset();
    }

    @Override
    public boolean markSupported() {
        return content.markSupported();
    }
}
