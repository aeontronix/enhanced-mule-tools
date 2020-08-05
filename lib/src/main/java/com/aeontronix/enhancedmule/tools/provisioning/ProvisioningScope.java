/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning;

import com.aeontronix.enhancedmule.tools.Environment;
import com.aeontronix.enhancedmule.tools.NotFoundException;
import com.kloudtek.util.UnexpectedException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProvisioningScope {
    private Type type;
    private String scope;

    public ProvisioningScope() {
    }

    public ProvisioningScope(Type type, String scope) {
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
        switch (type) {
            case ENV_ALL:
                return new HashSet<>(environments);
            case ENV:
                s = s.filter(e -> scope.equals(e.getName()));
                break;
            case ENV_TYPE:
                s = s.filter(e -> e.getType().equals(Environment.Type.valueOf(scope.toUpperCase())));
                break;
            case ENV_RGX:
                s = s.filter(e -> scope.matches(scope));
                break;
            case ENV_GROUP:
                s = s.filter(e -> e.getGroup() != null && e.getGroup().equals(scope));
                break;
            case ENV_NONPROD:
                s = s.filter(e -> !e.getType().equals(Environment.Type.PRODUCTION));
                break;
            default:
                throw new UnexpectedException("Invalid scope: "+type);
        }
        return s.collect(Collectors.toSet());
    }

    public String toShortMarkdown() {
        switch (type) {
            case ENV:
                return scope;
            case ENV_RGX:
                return "Regex("+scope+")";
            case ENV_TYPE:
                return "Type("+scope+")";
            case ENV_GROUP:
                return "Group("+scope+")";
            case ENV_NONPROD:
                return "Non-Prod";
            case ENV_ALL:
                return "*All*";
            default:
                return type+"("+scope+")";
        }
    }

    public enum Type {
        ENV, ENV_RGX, ENV_TYPE, ENV_ALL, ENV_GROUP, ENV_NONPROD
    }
}
