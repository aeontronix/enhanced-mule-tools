/*
 * Copyright (c) Aeontronix 2023
 */

package com.aeontronix.enhancedmule.tools.cli;

import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.enhancedmule.tools.anypoint.User;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.restclient.RESTClientHost;
import com.aeontronix.restclient.RESTResponse;
import org.slf4j.Logger;
import picocli.CommandLine;

import java.util.concurrent.Callable;

import static org.slf4j.LoggerFactory.getLogger;

@CommandLine.Command(name = "userinfo", description = "Retrieve user info from server")
public class UserInfoCmd extends AbstractCommand implements Callable<Integer> {
    private static final Logger logger = getLogger(UserInfoCmd.class);

    @Override
    public Integer call() throws Exception {
        final User user;
        try {
            final RESTClientHost anypointRestClient = getCli().getEMClient().getLegacyAnypointClient().getAnypointRestClient();
            try (RESTResponse response = anypointRestClient.get("/accounts/api/me").execute()) {
                logger.info("Anypoint User Info: " + IOUtils.toString(response.getContentStream()));
            }
        } catch (HttpException e) {
            if (e.getStatusCode() == 401) {
                logger.info("Authentication failed");
            }
        }
        return 0;
    }
}
