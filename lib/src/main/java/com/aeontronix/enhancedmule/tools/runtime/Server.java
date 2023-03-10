/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.runtime;

import com.aeontronix.enhancedmule.tools.anypoint.AnypointObject;
import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.kryptotek.DigestAlgorithm;
import com.aeontronix.kryptotek.DigestUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.aeontronix.commons.Hex;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Server extends AnypointObject<Environment> {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    protected String id;
    protected String name;

    public Server() {
    }

    public Server(Environment environment) {
        super(environment);
    }

    public Server(Environment environment, String id) {
        super(environment);
        this.id = id;
    }

    @JsonProperty
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void delete() throws HttpException {
        client.getHttpHelper().anypointHttpDelete("/hybrid/api/v1/servers/" + id, parent);
    }

    public boolean checkApplicationExist(@NotNull String name, @NotNull File file, boolean matchDigest) throws HttpException, IOException {
        try {
            HApplication application = findApplication(name);
            if (application != null) {
                return !matchDigest || application.matchDigest(new String(Hex.encodeHex(DigestUtils.digest(file, DigestAlgorithm.SHA1))));
            } else {
                return true;
            }
        } catch (NotFoundException e) {
            return false;
        }
    }

    public List<HApplication> listApplication() throws HttpException {
        String json = httpHelper.anypointHttpGet("/hybrid/api/v1/applications?targetId=" + id, parent);
        return jsonHelper.readJsonList(HApplication.class, json, this, "/data");
    }

    public HApplication findApplication(String name) throws NotFoundException, HttpException {
        for (HApplication application : listApplication()) {
            if (name.equals(application.getName())) {
                return application;
            }
        }
        throw new NotFoundException("Unable to find application " + name + " in server " + parent.getId());
    }
}
