/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning;

import com.aeontronix.enhancedmule.tools.Environment;
import com.aeontronix.enhancedmule.tools.NotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RolePermissionScope {
    private Type type;
    private String scope;

    public RolePermissionScope() {
    }

    public RolePermissionScope(Type type, String scope) {
        this.type = type;
        this.scope = scope;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Set<Environment> matchEnvironments(Collection<Environment> environments) throws NotFoundException {
        Stream<Environment> s = environments.stream();
        if( type == Type.ENV ) {
            s = s.filter(e -> scope.equals(e.getName()));
        } else if( type == Type.ENV_TYPE ) {
            s = s.filter(e -> e.getType().equals(Environment.Type.valueOf(scope.toUpperCase())));
        } else if( type == Type.ENV_RGX ) {
            s = s.filter(e -> scope.matches(scope));
        }
        return s.collect(Collectors.toSet());
    }

    public enum Type {
        ENV, ENV_RGX, ENV_TYPE
    }
}
