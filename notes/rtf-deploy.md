Checked if app is there

https://anypoint.mulesoft.com/exchange/api/v1/assets/64b57710-2b6e-4bce-b1d1-8c47e136a36d/batch-status-1.0.0-SNAPSHOT-mule-application?_=1580869612630

It's not so uploaded

https://maven.anypoint.mulesoft.com/api/v1/organizations/64b57710-2b6e-4bce-b1d1-8c47e136a36d/maven/64b57710-2b6e-4bce-b1d1-8c47e136a36d/batch-status-1.0.0-SNAPSHOT-mule-application/1.0.0/batch-status-1.0.0-SNAPSHOT-mule-application-1.0.0-mule-application.jar?_=1580869612745

note: must also upload POM, supports SNAPSHOT version but can't overwrite it

it includes X-ANYPNT-ENV-ID and ORG-ID

# Runtime versions

get runtime versions by gettings targets

https://anypoint.mulesoft.com/runtimefabric/api/organizations/b69aaacc-1d0d-4824-9780-d6c8a44aabd5/targets

which returns

```
[{"id":"6ef5ea67-9468-44cb-ad11-82b2be6c6fbd","name":"Development Fabric (ap-rtfv-d)","type":"runtime-fabric","runtimes":[{"type":"mule","versions":[{"baseVersion":"4.3.0","tag":"20201021"},{"baseVersion":"4.2.2","tag":"20201020"},{"baseVersion":"4.2.1","tag":"20201020"},{"baseVersion":"4.2.0","tag":"20201020"},{"baseVersion":"4.1.6","tag":"20201019"},{"baseVersion":"4.1.5","tag":"20201019"},{"baseVersion":"4.1.4","tag":"20201019"},{"baseVersion":"4.1.3","tag":"v1.2.65"},{"baseVersion":"4.1.2","tag":"v1.2.80"},{"baseVersion":"3.9.4","tag":"20201019"},{"baseVersion":"3.9.3","tag":"20201019"},{"baseVersion":"3.9.2","tag":"20201019"},{"baseVersion":"3.9.1","tag":"20201019"},{"baseVersion":"3.8.7","tag":"v1.2.41"}]}],"status":"Active","environments":["3d3470be-a891-4973-8de4-013916709aca","f1f0408d-9a71-415f-968f-c315dd39e4b5","c0c16ca6-c825-4b2f-9802-8f381c0fa4eb","6a0663a0-92e0-4ce8-ad43-a4d1bef1493e","ade4d015-54d3-4585-a338-5d5f5693b9d3","0da685e6-e0b1-49dc-a100-6bc4ac54c9f6","315a344c-e012-4eb8-b9b4-f1e046086a0b","9a54527a-64e4-475f-a246-c7bdc8bdeb0c"],"isAvailableForDeployments":true,"nodes":[{"id":"6ef5ea67-9468-44cb-ad11-82b2be6c6fbd","location":"us-east-1"}],"defaults":{"MuleApplication":{"sidecars":{"anypoint-monitoring":{"image":"auto","resources":{"cpu":{"reserved":"0m","limit":"50m"},"memory":{"reserved":"50Mi","limit":"50Mi"}}}}},"ApiQueryApplication":{"sidecars":{"anypoint-monitoring":{"image":"auto","resources":{"cpu":{"reserved":"0m","limit":"50m"},"memory":{"reserved":"50Mi","limit":"50Mi"}}}}}},"enhancedSecurity":false},{"id":"f62e82ad-456f-4a9f-9198-3ead66d24eb7","name":"Production Fabric (ap-rtfv-p)","type":"runtime-fabric","runtimes":[{"type":"mule","versions":[{"baseVersion":"4.3.0","tag":"20201021"},{"baseVersion":"4.2.2","tag":"20201020"},{"baseVersion":"4.2.1","tag":"20201020"},{"baseVersion":"4.2.0","tag":"20201020"},{"baseVersion":"4.1.6","tag":"20201019"},{"baseVersion":"4.1.5","tag":"20201019"},{"baseVersion":"4.1.4","tag":"20201019"},{"baseVersion":"4.1.3","tag":"v1.2.65"},{"baseVersion":"4.1.2","tag":"v1.2.80"},{"baseVersion":"3.9.4","tag":"20201019"},{"baseVersion":"3.9.3","tag":"20201019"},{"baseVersion":"3.9.2","tag":"20201019"},{"baseVersion":"3.9.1","tag":"20201019"},{"baseVersion":"3.8.7","tag":"v1.2.41"}]}],"status":"Active","environments":["f3d239d2-8f08-4d3b-bb1d-08e1713e0711","cbd65559-e619-4723-9e57-e64526a5a464","059b3414-198f-4b35-8e80-c849ae70257a"],"isAvailableForDeployments":true,"nodes":[{"id":"f62e82ad-456f-4a9f-9198-3ead66d24eb7","location":"us-east-1"}],"defaults":{"MuleApplication":{"sidecars":{"anypoint-monitoring":{"image":"auto","resources":{"cpu":{"reserved":"0m","limit":"50m"},"memory":{"reserved":"50Mi","limit":"50Mi"}}}}},"ApiQueryApplication":{"sidecars":{"anypoint-monitoring":{"image":"auto","resources":{"cpu":{"reserved":"0m","limit":"50m"},"memory":{"reserved":"50Mi","limit":"50Mi"}}}}}},"enhancedSecurity":false}]
```


----


POST https://anypoint.mulesoft.com/hybrid/api/v2/organizations/64b57710-2b6e-4bce-b1d1-8c47e136a36d/environments/ad3af7e1-6994-4979-9a87-ae860dd57e64/deployments?_=1580869636093


```json
{
  "name": "batch-status",
  "labels": [
    "beta"
  ],
  "target": {
    "provider": "MC",
    "targetId": "00334895-d7af-4e49-9272-14ac4b264bb0",
    "deploymentSettings": {
      "resources": {
        "cpu": {
          "reserved": "20m",
          "limit": "7500m"
        },
        "memory": {
          "reserved": "700Mi",
          "limit": "700Mi"
        }
      },
      "clustered": true,
      "http": {
        "inbound": {
          "publicUrl": "development.internal.api.panorama.hr.a2z.com/batch-status"
        }
      },
      "jvm": {
        "args": "-Dblabla=blalalala"
      },
      "runtimeVersion": "4.2.2:v1.2.16",
      "lastMileSecurity": true,
      "updateStrategy": "recreate"
    },
    "replicas": 2
  },
  "application": {
    "ref": {
      "groupId": "64b57710-2b6e-4bce-b1d1-8c47e136a36d",
      "artifactId": "batch-status-1.0.0-SNAPSHOT-mule-application",
      "version": "1.0.0",
      "packaging": "jar"
    },
    "assets": [],
    "desiredState": "STARTED",
    "configuration": {
      "mule.agent.application.properties.service": {
        "applicationName": "batch-status",
        "properties": {
          "foo": "bar"
        }
      }
    }
  }
}
```

minimal / default

```
{
  "name": "deletemenow",
  "labels": [
    "beta"
  ],
  "target": {
    "provider": "MC",
    "targetId": "6ef5ea67-9468-44cb-ad11-82b2be6c6fbd",
    "deploymentSettings": {
      "resources": {
        "cpu": {
          "reserved": "20m",
          "limit": "1700m"
        },
        "memory": {
          "reserved": "700Mi",
          "limit": "700Mi"
        }
      },
      "clustered": false,
      "enforceDeployingReplicasAcrossNodes": false,
      "http": {
        "inbound": {
          "publicUrl": "ap-rtfv-d.stanford.edu/deletemenow"
        }
      },
      "jvm": {},
      "runtimeVersion": "4.3.0:20201021",
      "lastMileSecurity": false,
      "forwardSslSession": false,
      "updateStrategy": "rolling"
    },
    "replicas": 1
  },
  "application": {
    "ref": {
      "groupId": "b69aaacc-1d0d-4824-9780-d6c8a44aabd5",
      "artifactId": "net-tools-api-2.1.0-mule-application",
      "version": "1.0.13",
      "packaging": "jar"
    },
    "assets": [],
    "desiredState": "STARTED",
    "configuration": {
      "mule.agent.application.properties.service": {
        "applicationName": "deletemenow",
        "properties": {},
        "secureproperties": {}
      }
    }
  }
}
```


---- CURRENT TEST JSON ---


```
{
  "application": {
    "desiredState": "STARTED",
    "ref": {
      "groupId": "b69aaacc-1d0d-4824-9780-d6c8a44aabd5",
      "artifactId": "net-tools-api-2.1.0-mule-application",
      "packaging": "jar",
      "version": "1.0.13"
    },
    "configuration": {
      "mule.agent.application.properties.service": {
        "secureproperties": {},
        "applicationName": "net-tools-api-2.1.0-mule-application",
        "properties": {}
      }
    }
  },
  "name": "deleteme",
  "labels": [
    "beta"
  ],
  "target": {
    "targetId": "6ef5ea67-9468-44cb-ad11-82b2be6c6fbd",
    "provider": "MC",
    "replicas": 1,
    "deploymentSettings": {
      "jvm": {},
      "updateStrategy": "rolling",
      "runtimeVersion": null,
      "clustered": false,
      "forwardSslSession": false,
      "resources": {
        "memory": {
          "reserved": "700Mi",
          "limit": "700Mi"
        },
        "cpu": {
          "reserved": "20m",
          "limit": "1700m"
        }
      },
      "lastMileSecurity": false,
      "enforceDeployingReplicasAcrossNodes": false
    }
  }
}
```

something in this breaks RTF:

```
{
  "id": "16e9925a-51a2-4d95-b4a5-e6b5c2a81704",
  "name": "deletemenow",
  "creationDate": 1606373805275,
  "lastModifiedDate": 1606373805275,
  "target": {
    "provider": "MC",
    "targetId": "6ef5ea67-9468-44cb-ad11-82b2be6c6fbd",
    "deploymentSettings": {
      "jvm": {},
      "sidecars": {
        "anypoint-monitoring": {
          "image": "auto",
          "resources": {
            "cpu": {
              "limit": "50m",
              "reserved": "0m"
            },
            "memory": {
              "limit": "50Mi",
              "reserved": "50Mi"
            }
          }
        }
      },
      "clustered": false,
      "resources": {
        "cpu": {
          "limit": "1700m",
          "reserved": "20m"
        },
        "memory": {
          "limit": "700Mi",
          "reserved": "700Mi"
        }
      },
      "runtimeVersion": "4.3.0:20201021",
      "updateStrategy": "rolling",
      "lastMileSecurity": false,
      "forwardSslSession": false,
      "enforceDeployingReplicasAcrossNodes": false
    },
    "replicas": 1
  },
  "status": "APPLIED",
  "application": {
    "status": "RUNNING",
    "desiredState": "STARTED",
    "ref": {
      "groupId": "b69aaacc-1d0d-4824-9780-d6c8a44aabd5",
      "artifactId": "net-tools-api-2.1.0-mule-application",
      "version": "1.0.13",
      "packaging": "jar"
    },
    "configuration": {
      "mule.agent.application.properties.service": {
        "properties": {},
        "applicationName": "deletemenow"
      }
    }
  },
  "desiredVersion": "24f0db77-a0b1-4d6c-8a87-c6468f8a9ec7",
  "replicas": [
    {
      "state": "STARTED",
      "deploymentLocation": "6ef5ea67-9468-44cb-ad11-82b2be6c6fbd",
      "currentDeploymentVersion": "24f0db77-a0b1-4d6c-8a87-c6468f8a9ec7",
      "reason": ""
    }
  ],
  "lastSuccessfulVersion": "24f0db77-a0b1-4d6c-8a87-c6468f8a9ec7"
}
```

COMPARE:

MANUAL

```
{
  "name": "deletemenow",
  "labels": [
    "beta"
  ],
  "target": {
    "provider": "MC",
    "targetId": "6ef5ea67-9468-44cb-ad11-82b2be6c6fbd",
    "deploymentSettings": {
      "resources": {
        "cpu": {
          "reserved": "20m",
          "limit": "1700m"
        },
        "memory": {
          "reserved": "700Mi",
          "limit": "700Mi"
        }
      },
      "clustered": false,
      "enforceDeployingReplicasAcrossNodes": false,
      "http": {
        "inbound": {
          "publicUrl": null
        }
      },
      "jvm": {},
      "runtimeVersion": "4.3.0:20201021",
      "lastMileSecurity": false,
      "forwardSslSession": false,
      "updateStrategy": "rolling"
    },
    "replicas": 1
  },
  "application": {
    "ref": {
      "groupId": "b69aaacc-1d0d-4824-9780-d6c8a44aabd5",
      "artifactId": "net-tools-api-2.1.0-mule-application",
      "version": "1.0.13",
      "packaging": "jar"
    },
    "assets": [],
    "desiredState": "STARTED",
    "configuration": {
      "mule.agent.application.properties.service": {
        "applicationName": "deletemenow",
        "properties": {},
        "secureproperties": {}
      }
    }
  }
}
```
