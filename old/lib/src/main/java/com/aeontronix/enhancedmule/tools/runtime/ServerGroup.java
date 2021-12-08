/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.runtime;

import com.aeontronix.enhancedmule.tools.anypoint.Environment;
import com.aeontronix.enhancedmule.tools.util.HttpException;

import java.util.Map;

public class ServerGroup extends Server {
    public ServerGroup() {
    }

    public ServerGroup(Environment environment) {
        super(environment);
    }

    public ServerGroup(Environment environment, String id) {
        super(environment, id);
    }

    @Override
    public void delete() throws HttpException {
        httpHelper.anypointHttpDelete("/hybrid/api/v1/serverGroups/" + id, parent);
    }

    public void addServer(Server server) throws HttpException {
        addServer(server.getId());
    }

    public void addServer(String serverId) throws HttpException {
        Map<String, Object> request = jsonHelper.buildJsonMap().set("serverGroupId", id).set("serverId", serverId).toMap();
        httpHelper.anypointHttpPost("/hybrid/api/v1/serverGroups/" + id + "/servers/" + serverId, request, parent);
    }
}
