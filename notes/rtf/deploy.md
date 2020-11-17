POST https://anypoint.mulesoft.com/hybrid/api/v2/organizations/b69aaacc-1d0d-4824-9780-d6c8a44aabd5/environments/0da685e6-e0b1-49dc-a100-6bc4ac54c9f6/deployments?_=1604187270181

```
{
  "name": "deleteme",
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
      "clustered": true,
      "enforceDeployingReplicasAcrossNodes": true,
      "http": {
        "inbound": {
          "publicUrl": "ap-rtfv-d.stanford.edu/deleteme"
        }
      },
      "jvm": {
        "args": "-XX:+UseG1GC"
      },
      "runtimeVersion": "4.3.0:v1.2.80",
      "lastMileSecurity": true,
      "forwardSslSession": true,
      "updateStrategy": "rolling"
    },
    "replicas": 2
  },
  "application": {
    "ref": {
      "groupId": "b69aaacc-1d0d-4824-9780-d6c8a44aabd5",
      "artifactId": "net-tools-v2.1.0",
      "version": "1.0.0",
      "packaging": "jar"
    },
    "assets": [],
    "desiredState": "STARTED",
    "configuration": {
      "mule.agent.application.properties.service": {
        "applicationName": "deleteme",
        "properties": {
          "foo": "bar"
        },
        "secureproperties": {}
      }
    }
  }
}
```

returns

```
{
  "id": "d052fd71-382d-4b13-b836-f9afc5c0bd16",
  "name": "deleteme",
  "creationDate": 1604187270437,
  "lastModifiedDate": 1604187270437,
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
      "clustered": true,
      "enforceDeployingReplicasAcrossNodes": true,
      "http": {
        "inbound": {
          "publicUrl": "ap-rtfv-d.stanford.edu/deleteme"
        }
      },
      "jvm": {
        "args": "-XX:+UseG1GC"
      },
      "runtimeVersion": "4.3.0:v1.2.80",
      "lastMileSecurity": true,
      "forwardSslSession": true,
      "updateStrategy": "rolling"
    },
    "replicas": 2
  },
  "status": "UNDEPLOYED",
  "application": {
    "status": "NOT_RUNNING",
    "desiredState": "STARTED",
    "ref": {
      "groupId": "b69aaacc-1d0d-4824-9780-d6c8a44aabd5",
      "artifactId": "net-tools-v2.1.0",
      "version": "1.0.0",
      "packaging": "jar"
    },
    "configuration": {
      "mule.agent.application.properties.service": {
        "applicationName": "deleteme",
        "properties": {
          "foo": "bar"
        }
      }
    }
  },
  "desiredVersion": null,
  "replicas": [],
  "lastSuccessfulVersion": null
}
```

update: 

PATCH https://anypoint.mulesoft.com/hybrid/api/v2/organizations/b69aaacc-1d0d-4824-9780-d6c8a44aabd5/environments/0da685e6-e0b1-49dc-a100-6bc4ac54c9f6/deployments/834c6590-db3c-4271-8a49-27d9f38b0e5a?_=1604188689200

```
{
  "id": "834c6590-db3c-4271-8a49-27d9f38b0e5a",
  "name": "deleteme",
  "creationDate": 1604188579855,
  "lastModifiedDate": 1604188579855,
  "target": {
    "provider": "MC",
    "targetId": "6ef5ea67-9468-44cb-ad11-82b2be6c6fbd",
    "deploymentSettings": {
      "jvm": {
        "args": "-XX:+UseG1GC"
      },
      "http": {
        "inbound": {
          "publicUrl": "ap-rtfv-d.stanford.edu/deleteme"
        }
      },
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
      "clustered": true,
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
      "runtimeVersion": "4.3.0:v1.2.80",
      "updateStrategy": "rolling",
      "lastMileSecurity": true,
      "forwardSslSession": true,
      "enforceDeployingReplicasAcrossNodes": true
    },
    "replicas": 2,
    "type": "MC",
    "status": "ACTIVE"
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
        "applicationName": "deleteme",
        "properties": {
          "foo": "bar"
        },
        "secureproperties": {}
      }
    }
  },
  "desiredVersion": "03710b89-a44b-4747-9f99-8ae4724c8134",
  "replicas": [
    {
      "state": "STARTED",
      "deploymentLocation": "6ef5ea67-9468-44cb-ad11-82b2be6c6fbd",
      "currentDeploymentVersion": "03710b89-a44b-4747-9f99-8ae4724c8134",
      "reason": ""
    },
    {
      "state": "STARTED",
      "deploymentLocation": "6ef5ea67-9468-44cb-ad11-82b2be6c6fbd",
      "currentDeploymentVersion": "03710b89-a44b-4747-9f99-8ae4724c8134",
      "reason": ""
    }
  ],
  "lastSuccessfulVersion": "03710b89-a44b-4747-9f99-8ae4724c8134"
}
```
