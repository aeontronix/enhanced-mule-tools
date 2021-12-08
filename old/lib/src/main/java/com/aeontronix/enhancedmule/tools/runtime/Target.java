/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.runtime;

import java.util.List;

public class Target {
    private String id;
    private String name;
    private String type;
    private List<Runtime> runtimes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Runtime findRuntimeByType(String type) {
        if( runtimes != null ) {
            for (Runtime runtime : runtimes) {
                if( type.equals(runtime.type) ) {
                    return runtime;
                }
            }
        }
        return null;
    }

    public List<Runtime> getRuntimes() {
        return runtimes;
    }

    public void setRuntimes(List<Runtime> runtimes) {
        this.runtimes = runtimes;
    }

    public static class Runtime {
        private String type;
        private List<RuntimeVersion> versions;

        public Runtime() {
        }

        public Runtime(String type, List<RuntimeVersion> versions) {
            this.type = type;
            this.versions = versions;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<RuntimeVersion> getVersions() {
            return versions;
        }

        public void setVersions(List<RuntimeVersion> versions) {
            this.versions = versions;
        }
    }

    public static class RuntimeVersion {
        private String baseVersion;
        private String tag;

        public RuntimeVersion() {
        }

        public RuntimeVersion(String baseVersion) {
            this.baseVersion = baseVersion;
        }

        public String getBaseVersion() {
            return baseVersion;
        }

        public void setBaseVersion(String baseVersion) {
            this.baseVersion = baseVersion;
        }

        public String getTag() {
            return tag;
        }

        public void setTag(String tag) {
            this.tag = tag;
        }
    }
}
