---
layout: page
title: Automated API Management
permalink: /automated-api-management.html
nav_order: 2
---

# API Management Automation

# Introduction

Anypoint tools is able to automatically setup all API Management configuration for an API when it deploys it.

In order to use this feature, you will need to add an anypoint.json, anypoint.yml or anypoint.yaml file to your project.

You will then be able to add all the API Management configuration to that file. The following documentation will be based
 on a JSON file, but you also are able to use the equivalent in YAML.

# Anypoint configuration file

For the full schema definition, see [this page](anypoint-json-descriptor.html), or for the raw JSON schema see 
[here](../../lib/src/main/resources/anypoint.schema.json)

## Application parameters

By default the application id will be assigned to be the same as your project's pom.xml artifact id, but you can also 
override it as shown below:

```json
{
  "id": "my-application-name"
}
```

## API Definition

In order to have your API provisioned with API Manager, you will need to create an "api" section in your anypoint.json as shown below:

```json
{
  "api": {
  }
}
```

Then you will need to specify where in exchange your API specifications are (do keep in mind they need to be in the same 
organization and business group as where you deploy your application )

### Automatic API asset discovery

Using this approach (requires anypoint studio 7.4), you add your dependencies to your project as a maven dependency. If 
you use this, the 'process-descriptor' maven goal will automatically detect the maven dependency and use it.

*Note: Since anypoint tools only support one API per application at this time, it requires one and only one maven dependency
to exist (otherwise it will skip this step and you'll have to specify the dependency manually)

### Manual API asset

If you're not using the maven-based API approach, you will need to manually specify the api assetId and assetVersion (
the groupId cannot be set since deployment only supports APIs within the same group)

```json
{
  "api": {
    "assetId": "my-api",
    "assetVersion": "1.0.0"    
  }
}
```

If not set, assetId will default to "[application-id]-api" and the assetVersion to your pom version (excluding `-SNAPSHOT` if present)

### Auto-Create: HTTP API

If your API doesn't exist in Exchange (or in the specific group you're deploying to), anypoint tools can automatically
create the exchange asset using HTTP type, by specify the 'create' attribute with the value of 'http'

```json
{
  "api": {
    "type": "http",
    "create": true,
    "assetId": "my-api",
    "assetVersion": "1.0.0"    
  }
}
```

### Other API configuration attributes

In addition to assetId and assetVersion you can set the following attributes:

* endpoint: The API endpoint
* label: The API label

```json
{
  "api": {
    "endpoint": "http://myapi/v1/", 
    "label": "prc"
  }
}
```

## Policies

You can add policies to your configuration file as shown below:

```json
{
  "api": {
    "assetId": "my-api",
    "assetVersion": "1.0.0",  
    "endpoint": "http://myapi/xxx",
    "policies": [
        {
          "groupId": "68ef9520-24e9-4cf2-b2f5-620025690913",
          "assetId": "header-injection",
          "assetVersion": "1.0.0",
          "policyTemplateId": "59",
          "type": "json",
          "configurationData": {
            "inboundHeaders": [
              {
                "key": "foo",
                "value": "bar"
              }
            ]
          }
        }
      ]
  }
}
```

The type should always be JSON, but the rest you will need to obtain the parameters from anypoint and unfortunately 
there's no "easy" to do so. You will need to go to anypoint and use your web browser's debug capabilities ( for example 
Developer Tools in chrome ) and create your desired policy while it's recording network activity.

You should see it doing a POST to an url like: `https://anypoint.mulesoft.com/apimanager/api/v1/organizations/[yourorgid]/environments/{yourenvid}/apis/15694893/policies`

With a payload containing all the policy configuration, for example:

```json
{
  "configurationData": {
    "credentialsOriginHasHttpBasicAuthenticationHeader": "customExpression",
    "clientIdExpression": "#[attributes.headers['client_id']]",
    "clientSecretExpression": "#[attributes.headers['client_secret']]"
  },
  "id": null,
  "pointcutData": null,
  "policyTemplateId": 289307,
  "apiVersionId": 15694893,
  "groupId": "68ef9520-24e9-4cf2-b2f5-620025690913",
  "assetId": "client-id-enforcement",
  "assetVersion": "1.2.1"
}
```

Finally please note that if policies is non-null, all existing policies are deleted and replaced by whatever policies 
you've defined.

So this JSON will leave your policies as-is:

```json
{
  "api": {
    "assetId": "my-api",
    "assetVersion": "1.0.0"    
  }
}
```

but this JSON will delete all policies

```json
{
  "api": {
    "assetId": "my-api",
    "assetVersion": "1.0.0",
    "policies": []
  }
}
```
## SLA Tiers

You can specify what SLA Tiers should be configuration with the following JSON.

```json
{
  "api": {
    "assetId": "my-api",
    "assetVersion": "1.0.0"    
    "slaTiers": [
      {
        "name": "Premium",
        "description": "Premium level SLA tier",
        "autoApprove": false,
        "limits": [
          {
            "visible": true,
            "timePeriodInMilliseconds": 1000,
            "maximumRequests": 10
          }
        ]
      }
    ]
  }
}
```

## Application Client

You can automatically provision a client application by adding a `client` object to your anypoint descriptor

```json
{
  "client": {
    "name": "myclientapp-${organization.lname}-${environment.lname}",
    "description": "My Client Application",
    "url": "http://myapp"
  }
}
```

If you don't specify the name, it will be automatically generated based on the following pattern: 
`[app id]-${environment.lname}`.

So for example at a minimum you can just specify, UNLESS you've create a "Client Provider" in which case you need at least
one accessed API (see below)

```json
{
  "client": {
  }
}
```

## API Access

You can specify that the API's client be automatically given access to other APIs by specifying the exchange group id,
asset id and version for the APIs it should be granted access. If the API granted access to uses an SLA Tier that disables
automatic approval, the approval request will be automatically granted as well.

```json
{
  "client": {
    "access": [
      {
        "orgId": "68ef9520-24e9-4cf2-b2f5-620025690944",
        "groupId": "79238423-7259-8329-dk23-234273984723",
        "assetId": "otherapi",
        "assetVersion": "1.0.0",
        "label": "exp"
      }
    ]
  }  
}
```

If orgId is not set it will default to the API's organization, and if groupId is not set it will default to the orgId.

Please note that while historically the group id and the org id have been the same, that is no longer the case with newer
versions of the anypoint platform.

You can also specify that existing client applications be granted access to your API using `accessedBy` (you will
also be able to specify those through parameters when deploying).

```json
{
  "api": {
     "assetId": "my-api",
     "assetVersion": "1.0.0"    
     "accessedBy": ["someclient1-${environment.lname}","someclient2-${environment.lname}"]
  }
}
```

## <a name="variables"></a> Variables

When running the deployment or provisioning, you will be able to specify variables that will be automatically inserted 
using the expression ${varname}

For example example if your API URL varies and want to be able to specify it when you deploy you just could specify the
variable `apiUrl=http://myapp`, and in the anypoint descriptor just set:

```json
{
  "api": {
    "assetId": "my-api",
    "assetVersion": "1.0.0"    
    "endpoint": "${apiUrl}/v1/"
  }
}
```

A number of variables are also pre-defined and available for you to use:

* `app.id`: Application id
* `environment.id`: Environment Id
* `environment.name`: Environment name
* `environment.lname`: Environment name in lower case and with any spaces converted to underscores
* `organization.name`: Organization name
* `organization.lname`: Organization name in lower case and with any spaces converted to underscores

Additionally you can also use some functions. So for example let's say the you've assigned the variable 'myvar' 
to 'littleDog'

* Upper case: `${u:myvar}`

        This will convert varname to upper case (ie: `littledog`)

* Lower case: `${l:varname}`

        This will convert varname to lower case (ie: `LITTLEDOG`)

* Capitalize: `${c:varname}`

        This will capitalize varname (ie: `LittleDog`)

* Base 64 Encode: `${eb64:varname}`

        This will base 64 encode the variable (ie: `bGl0dGxlRG9n`)

* Base 64 Decode: `${db64:varname}`

        This will base 64 decode the variable

* Add prefix if variable is not empty: `${p:-:varname}`

        This will add a prefix ( a dash in example above ) to the string value only if the variable value is not empty.
        If you wish to use `:` a separator you will need to specify it twice (see example below)
        
        For example:
        
        varname='dog' and an expression `${p:-:varname}black` will result in: `dog-black`
        varname='' (or variable not set) and an expression `${p:-:varname}black` will result in: `black`
        varname='dog' and an expression `${p:_:varname}black` will result in: `dog_black`
        varname='dog' and an expression `${p::::varname}black` will result in: `dog:black`
        
* Add suffix if variable is not empty: `${s:-:varname}`

        This will add a suffix ( a dash in example above ) to the string value only if the variable value is not empty.
        If you wish to use `:` a separator you will need to specify it twice (see example below)
        
        For example:
        
        varname='dog' and an expression `my${s:-:varname}` will result in: `my-dog`
        varname='' (or variable not set) and an expression `my${s:-:varname}` will result in: `my`
        varname='dog' and an expression `my${s:_:varname}` will result in: `my_dog`
        varname='dog' and an expression `my${s::::varname}` will result in: `my:dog`
        
You can also nest variable expressions, for example let's say you defined the variables `myvar=varname` and 
`varname=dog`. You could then write `${${myvar}}` which would result in `dog`

# Multiple APIs

Specifying multiple APIs in the descriptor is planned but not implemented yet. Add a comment to this issue
if you need it:

[https://gitlab.com/aeontronix/oss/enhanced-mule-tools/issues/1](https://gitlab.com/aeontronix/oss/enhanced-mule-tools/issues/1)

# Properties injection

Several properties will be automatically generated and added to the application properties when it's deployed:

* `anypoint.api.id`: The API id generated for the API
* `anypoint.platform.client_id`: The environment platform client id
* `anypoint.platform.client_secret`: The environment platform client id
* `anypoint.api.client.id`: The client id for the generated application client
* `anypoint.api.client.secret`: The client secret for the generated application client
