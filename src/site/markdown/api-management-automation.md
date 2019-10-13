# Overview

The most important capability of anypoint-tools is the ability to automate API management.

In order to do so, you will need to create a JSON file (anypoint.json, which needs to be located alongside your xml flow files) containing the API configuration details. For example:

```json
{
  "name": "My API",
  "version": "1.0.0",
  "endpoint": "${url}/xxx",
  "policies": [
    {
      "groupId": "68ef9520-24e9-4cf2-b2f5-620025690913",
      "assetId": "header-injection",
      "assetVersion": "1.0.0",
      "policyTemplateId": "59",
      "type": "json",
      "data": {
        "inboundHeaders": [
          {
            "key": "foo",
            "value": "bar"
          }
        ]
      }
    }
  ],
  "access": [
    {
      "groupId": "68ef9520-24e9-4cf2-b2f5-620025690944",
      "assetId": "otherapi",
      "assetVersion": "1.0.0",
    }
  ]
}
```

So in this example, it will 

- Create a API named "My API" version "1.0.0"
- Assign to it a header-injection policy
- Create a client application for the API ( in the format of \[api name in lowercase]-\[org name in lowercase]-\[env name in lowercase] )
- Request API access to api "otherapi" for the client application created in previous step

Right now the only way to use API provisioning is through the deployment of an application through the maven plugin (in 
the future I'll make it so that it can be done in isolation).
