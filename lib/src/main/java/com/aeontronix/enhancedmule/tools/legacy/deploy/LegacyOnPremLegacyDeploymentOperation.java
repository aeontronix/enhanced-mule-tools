/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.legacy.deploy;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.anypoint.application.deploy.HDeploymentOperation;
import com.aeontronix.enhancedmule.tools.runtime.HDeploymentResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LegacyOnPremLegacyDeploymentOperation implements LegacyDeploymentOperation {
    private static final Logger logger = LoggerFactory.getLogger(LegacyOnPremLegacyDeploymentOperation.class);
    private final HDeploymentOperation request;

    public LegacyOnPremLegacyDeploymentOperation(HDeploymentOperation request) {
        this.request = request;
    }

    @Override
    public HDeploymentResult deploy(Environment environment, String appName, String filename, File file) {
//        Server target = new Server(null);
//        HttpHelper.MultiPartRequest request;
//        long start = System.currentTimeMillis();
//        HttpHelper httpHelper = target.getClient().getHttpHelper();
//        try {
//            logger.debug("Searching for pre-existing application named " + appName);
//            HApplication application = target.findApplication(appName);
//            logger.debug("Found application named {} : {}", appName, application.getId());
//            request = httpHelper.createMultiPartPatchRequest("/hybrid/api/v1/applications/" + application.getId(),
//                    target.getParent());
//        } catch (NotFoundException e) {
//            logger.debug("Couldn't find application named {}", appName);
//            request = httpHelper.createMultiPartPostRequest("/hybrid/api/v1/applications", target.getParent());
//        }
//        String json = request.addText("targetId", target.getId())
//                .addText("artifactName", appName)
//                .addBinary("file", new StreamSource() {
//            @Override
//            public String getFileName() {
//                return filename;
//            }
//
//            @Override
//            public InputStream createInputStream() throws IOException {
//                return new FileInputStream(file);
//            }
//        }).execute();
//        if (logger.isDebugEnabled()) {
//            logger.debug("File upload took " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds");
//        }
//        HApplication application = target.getClient().getJsonHelper().readJson(new HApplication(target), json, "/data");
//        return new HDeploymentResult(application);
        return null;
    }
}
