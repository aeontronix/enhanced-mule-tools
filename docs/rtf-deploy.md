Checked if app is there

https://anypoint.mulesoft.com/exchange/api/v1/assets/64b57710-2b6e-4bce-b1d1-8c47e136a36d/batch-status-1.0.0-SNAPSHOT-mule-application?_=1580869612630

It's not so uploaded

https://maven.anypoint.mulesoft.com/api/v1/organizations/64b57710-2b6e-4bce-b1d1-8c47e136a36d/maven/64b57710-2b6e-4bce-b1d1-8c47e136a36d/batch-status-1.0.0-SNAPSHOT-mule-application/1.0.0/batch-status-1.0.0-SNAPSHOT-mule-application-1.0.0-mule-application.jar?_=1580869612745

note: must also upload POM, supports SNAPSHOT version but can't overwrite it

it includes X-ANYPNT-ENV-ID and ORG-ID


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
