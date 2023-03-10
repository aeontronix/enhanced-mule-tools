/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.util;

import com.aeontronix.commons.StringUtils;
import com.aeontronix.commons.exception.UnexpectedException;
import com.aeontronix.enhancedmule.tools.anypoint.LegacyAnypointClient;
import com.aeontronix.enhancedmule.tools.anypoint.authentication.AuthenticationProvider;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HttpHelperRecorder extends HttpHelper {
    private HttpHelperRecording recording = new HttpHelperRecording();

    public HttpHelperRecorder(LegacyAnypointClient client, AuthenticationProvider authenticationProvider, String orgName) {
        super(client.getJsonHelper(), authenticationProvider);
        recording.setOrgName(orgName);
    }

    public HttpHelperRecording getRecording() {
        return recording;
    }

    @Override
    protected String executeWrapper(@NotNull HttpRequestBase method, MultiPartRequest multiPartRequest) throws HttpException {
        HttpHelperOperation op = new HttpHelperOperation(method.getMethod(), method.getURI().toString());
        recording.addOperation(op);
        if (method instanceof HttpEntityEnclosingRequestBase) {
            HttpEntity entity = ((HttpEntityEnclosingRequestBase) method).getEntity();
            if (entity != null && entity.isRepeatable()) {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                try {
                    entity.writeTo(buf);
                } catch (IOException e) {
                    throw new UnexpectedException(e);
                }
                op.setContent(StringUtils.base64EncodeToString(buf.toByteArray()));
            }
        }
        String json = super.executeWrapper(method, multiPartRequest);
        op.setResult(json);
        return json;
    }
}
