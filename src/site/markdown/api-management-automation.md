# API Management Automation

# Introduction

Anypoint tools is able to automatically setup all API Management configuration for an API when it deploys it.

In order to use this feature, you will need to add an anypoint.json, anypoint.yml or anypoint.yaml file to your project.

By default you can add that file to the src/main/mule folder, but you could add it instead to the root of the project
by adding the following to your pom.xml (or any directory or your choose by replacing `${project.basedir}`).

```xml
<project>
	<build>
        ...
		<resources>
			<resource>
				<directory>${project.basedir}</directory>
				<includes>
					<include>anypoint.json</include>
					<include>anypoint.yml</include>
					<include>anypoint.yaml</include>
				</includes>
			</resource>
		</resources>
        ...
    </build>
</project>
```

You will then be able to add all the API Management configuration to that file. The following documentation will be based
 on a JSON file, but you also are able to use the equivalent in YAML.

# Anypoint configuration file

## Full JSON Schema

JSON Schema: [schema/anypoint-schema.schema.html](schema/anypoint-schema.schema.html)

Raw JSON Schema: [schema/anypoint-schema.schema.json](schema/anypoint-schema.schema.json)

## API Definition

First you will need to create an entry for your API and include the API name and version using the following format

```json
{
  "api": {
    "name": "My API",
    "version": "1.0.0"    
  }
}
```

Additionally you can specify the following optional attributes:

* endpoint: The API endpoint
* label: The API label

```json
{
  "api": {
    "name": "My API",
    "version": "1.0.0",    
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
    "name": "My API",
    "version": "1.0.0",    
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
    "name": "My API",
    "version": "1.0.0"    
  }
}
```

but this JSON will delete all policies

```json
{
  "api": {
    "name": "My API",
    "version": "1.0.0",
    "policies": []
  }
}
```

## Application Client

A client application will automatically be created for your API. 

You can disable that behavior by setting `createClientApplication`

```json
{
  "api": {
    "name": "My API",
    "version": "1.0.0",    
    "createClientApplication": false
  }
}
```

The name will be automatically generated based on the following pattern (see variables): `${api.lname}-${organization.lname}-${environment.lname}`

You can however override this or set the client application description and url using the following json:

```json
{
  "api": {
    "name": "My API",
    "version": "1.0.0",    
    "clientApp": {
      "name": "myclientapp-${organization.lname}-${environment.lname}",
      "description": "My Client Application",
      "url": "http://myapp"
    }
  }
}
```

## SLA Tiers

You can specify what SLA Tiers should be configuration with the following JSON.

```json
{
  "api": {
    "name": "My API",
    "version": "1.0.0",    
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

## API Access

You can specify that the API's client be automatically given access to other APIs by specifying the exchange group id,
asset id and version for the APIs it should be granted access. If the API granted access to uses an SLA Tier that disables
automatic approval, the approval request will be automatically granted as well.

```json
{
  "api": {
    "name": "My API",
    "version": "1.0.0",    
    "access": [
      {
        "groupId": "68ef9520-24e9-4cf2-b2f5-620025690944",
        "assetId": "otherapi",
        "assetVersion": "1.0.0",
        "label": "exp"
      }
    ]
  }
}
```

You can also specify that existing client applications be granted access to your API using `accessedBy` (you will
also be able to specify those through parameters when deploying).

```json
{
  "api": {
    "name": "My API",
    "version": "1.0.0",    
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
    "name": "My API",
    "version": "1.0.0",    
    "endpoint": "${apiUrl}/v1/"
  }
}
```

A number of variables are also pre-defined and available for you to use:

* `environment.id`: Environment Id
* `environment.name`: Environment name
* `environment.lname`: Environment name in lower case and with any spaces converted to underscores
* `organization.name`: Organization name
* `organization.lname`: Organization name in lower case and with any spaces converted to underscores
* `api.name`: API name
* `api.lname`: API name in lower case and with any spaces converted to underscores

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

[https://gitlab.com/aeontronix/oss/anypoint-tools/issues/1](https://gitlab.com/aeontronix/oss/anypoint-tools/issues/1)

# Properties injection

Several properties will be automatically generated and added to the application properties when it's deployed:

* `anypoint.api.id`: The API id generated for the API
* `anypoint.platform.client_id`: The environment platform client id
* `anypoint.platform.client_secret`: The environment platform client id
* `anypoint.api.client.id`: The client id for the generated application client
* `anypoint.api.client.secret`: The client secret for the generated application client
