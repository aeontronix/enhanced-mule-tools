---
layout: page
title: Maven Plugin Docs
permalink: /maven-plugin.html
nav_order: 3
---

# 1. Authentication

For any goal marked as 'authenticated' below, you will need to specify one of the following authentication credentials

## 1.1. Username / Password

If you have an user with username/password credentials (so NOT a Single Sign On user), you can just specify the following properties:

| Name | Property | Description |
|------|----------|-------------|
| username | anypoint.username | Anypoint Username
| password | anypoint.password | Anypoint Password

## 1.2. Connected Apps

You can also use a "connected apps" client id/secret (grant type must be `client_credentials`)

**IMPORTANT: At the time of this documentation being updated, there was a bug with connected apps client credential,
which causes client application create impossible**

| Name | Property | Description |
|------|----------|-------------|
| clientId | anypoint.client.id | Anypoint authentication client id
| clientSecret | anypoint.client.secret | Anypoint authentication client secret

## 1.3. Bearer Token

Alternatively you can obtain a user's authentication bearer token and pass it using the following attribute

| Name | Property | Description |
|------|----------|-------------|
| bearer | anypoint.bearer | Anypoint authentication bearer token

# 2. Goals List

The maven plugin supports the following goals:

| Name   | Description | Authenticated | Default Phase | 
|--------|-------------|---------------|---------------|
| [process-descriptor](#process-descriptor) | Process an application's anypoint descriptor and include in project | No | generate-resources
| [provision](#provision) | Provision anypoint | Yes | install
| [deploy](#deploy) | Deploy an a mule application through Anypoint Runtime Manager | Yes | deploy

## <a name="process-descriptor"></a>2.1. process-descriptor

You **MUST** run this goal when building your mule application. This will read an anypoint.json/yml/yaml file, process it
and generate a new anypoint.json file that is added to your project's generated resources folder, as well as attached as
an maven artifact with a classifier `anypoint-descriptor`.

| Name | Property | Required | Default | Description |
|------|----------|----------|---------|----|
| descriptor | anypoint.descriptor | | No | Descriptor file path
| mulePluginCompatibility | muleplugin.compat | | false | Must be set to true to use standard mule plugin to deploy
| attachDescriptor | anypoint.descriptor.attach | true | No | Attaches the descriptor to the maven project (don't change this unless you're using provisioning goal or have a really good reason to do so) 

## <a name="deploy"></a>2.2. deploy

The `deploy` goal of the maven plugin supports the following parameters for both pom-based and standalone usage.

- Name: name of the parameter when settings it in the `<configuration>` section of the pom.xml
- Property: name of the property used to set the parameter (this is the only way to set parameters when using the standalone invocation)
- Description: Description of the parameter
- Required: If the parameter is required

| Name | Property | Required |  Description |
|------|----------|----------|--------------|
| org | anypoint.org | Yes | Organization Name
| env | anypoint.env | Yes | Environment Name
| skipApiProvisioning | anypoint.api.provisioning.skip | No | If set to `true`, automatic API Manager provisioning will be skipped
| skipDeploy | anypoint.deploy.skip | No |If set to `true`, the deployment will be skipped
| filename | anypoint.deploy.filename | No | Filename (if not specified the file's name will be used)
| appName | anypoint.deploy.name | No | Application name ( default: `${artifactId}-${anypoint.env}` )
| force | anypoint.deploy.force | No | If true, will force deployment even if same already application was already deployed.
| skipWait | anypoint.deploy.skipwait | No | If true will skip wait for application to start
| deployTimeout | anypoint.deploy.timeout | No | Deployment timeout in milliseconds ( defaults to 10 min)
| deployRetryDelay | anypoint.deploy.retrydelay | No | Delay (in milliseconds) in retrying a deployment ( defaults to 2.5 seconds )
| properties | anypoint.deploy.properties.*[propertyname]* | No | Application Properties that will be set in Runtime Manager (note this must be in the form of a [maven properties](https://maven.apache.org/pom.html#Properties) ) 
| propertyfile | anypoint.deploy.propertyfile | No | Application Properties stored in a property file, that will be set in Runtime Manager
| fileProperties | | No | Properties that will be injected in a property file in the project's archive at deploy time (rather than in Runtime Manager)
| filePropertiesPath | | No | Path of the property file (inside the application jar) for properties set in `fileProperties`
| filePropertiesSecure | | No | If set to true, all secure properties will be moved into `fileProperties` rather than `properties`
| vars | | No | Variables for anypoint.json templating

When running standalone, the following parameters are also supported

| Name | Property | Required | Description |
|------|----------|----------|-------------|
| file | anypoint.deploy.file | Yes | File to deploy

When deploying to Cloudhub, the following parameters are also supported (all those values are optional):

| Name | Property | Description |
|------|----------|----------|
| muleVersionName | anypoint.deploy.ch.muleversion | Mule version name (will default to latest if not set)
| region | anypoint.deploy.ch.region | Deployment region ( Ignored if a VPC is used )
| workerType | anypoint.deploy.ch.worker.type | Worker type (will default to smallest if not specified). This is the same value as accepted by the mulesoft maven plugin, see [docs](https://docs.mulesoft.com/mule-runtime/4.2/mmp-concept#deploy-a-mule-application-to-cloudhub for full list)
| workerCount | anypoint.deploy.ch.worker.count | Worker count (defaults to 1)
| customlog4j | anypoint.deploy.ch.customlog4j | If set to 'true' the application's log4j will be used will be used instead of a cloudhub-managed log4j file (see [docs for more details](https://docs.mulesoft.com/runtime-manager/custom-log-appender)). (defaults to false)
| mergeExistingProperties | anypoint.deploy.mergeproperties | If not set or set to true, if there is already an application deployed with the same name, it will retrieve it's properties and merge them with this application's properties.
| mergeExistingPropertiesOverride | anypoint.deploy.mergeproperties.override | Indicates the behavior to use when merging conflicting properties when 'mergeExistingProperties' is used. If true it will override the existing property, or if false it will override it. (Defaults to false)
| persistentQueues | anypoint.deploy.persistentqueue | Enable persistent queues (defaults to false)
| persistentQueuesEncrypted | anypoint.deploy.persistentqueue.encrypted | Enable encryption for persistent queues (defaults to false)
| objectStoreV1 | anypoint.deploy.objectstorev1 | Set object store v1 instead of v2 (defaults to false)
| extMonitoring | anypoint.deploy.extMonitoring | Enable monitoring and visualizer (defaults to true)
| staticIPs | anypoint.deploy.staticips | Enable static ips (defaults to false)

When deploying to on-prem / Hybrid, the following parameters are also supported:

| Name | Property | Required | Description |
|------|----------|----------|-------------|
| target | anypoint.target | Yes | Anypoint target name (Server / Server Group / Cluster)

## <a name="process-descriptor"></a>2.3. provision

This goal is used to only perform the anypoint provisioning (without the deployment), and set properties for you use in
conjunction with the standard mule plugin.

Doing deployment using just provisioning has the downside of losing some capabilities, but on the upside it allows to use
the mule plugin to do provisioning which in some scenarios might be a better approach (for example if you want to deploy
to RTF which isn't yet supported by this plugin).

| Name | Property | Required | Description |
|------|----------|----------|-------------|
| org | anypoint.org | Yes | Organization Name
| env | anypoint.env | Yes | Environment Name
| skipApiProvisioning | anypoint.api.provisioning.skip | No | If set to `true`, automatic API Manager provisioning will be skipped
| vars | | No | Variables for anypoint.json templating

After the plugin runs, the following properties will be set

| Property | Description |
|----------|-------------|
|anypoint.api.id | API Id|
|anypoint.api.client.id| API client application id (if client provisioning is set| 
|anypoint.api.client.secret| API client application secret (if client provisioning is set| 
|anypoint.platform.client_id| Client id for anypoint environment
|anypoint.platform.client_secret| Client secret for anypoint environment (this will be only set if the user has the required access)

Important note: Due how weirdly the mule maven plugin is implemented, you will need to set process-descriptor to false.

Example configuration to deploy an application to cloudhub using provisioning-only

```xml
<build>
    <plugins>
        <plugin>
            <groupId>com.aeontronix.enhanced-mule</groupId>
            <artifactId>enhanced-mule-tools-maven-plugin</artifactId>
            <version>1.1.0</version>
            <executions>
                <execution>
                    <goals>
                        <goal>process-descriptor</goal>
                        <goal>provision</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <attachDescriptor>false</attachDescriptor>
            </configuration>
        </plugin>
        <plugin>
            <groupId>org.mule.tools.maven</groupId>
            <artifactId>mule-maven-plugin</artifactId>
            <version>${mule.maven.plugin.version}</version>
            <extensions>true</extensions>
            <executions>
                <execution>
                    <id>deploy</id>
                    <phase>deploy</phase>
                    <goals>
                        <goal>deploy</goal>
                    </goals>
                </execution>
            </executions>
            <configuration>
                <cloudHubDeployment>
                    <uri>https://anypoint.mulesoft.com</uri>
                    <muleVersion>${app.runtime}</muleVersion>
                    <username>${username.username}</username>
                    <password>${password.password}</password>
                    <applicationName>anypointtoolsdemo-provisiononly-${anypoint.env}</applicationName>
                    <environment>${anypoint.env}</environment>
                    <region>us-west-2</region>
                    <workers>1</workers>
                    <workerType>MICRO</workerType>
                    <properties>
                        <anypoint.api.id>${anypoint.api.id}</anypoint.api.id>
                        <anypoint.api.client.id>${anypoint.api.client.id}</anypoint.api.client.id>
                        <anypoint.api.client.secret>${anypoint.api.client.secret}</anypoint.api.client.secret>
                        <anypoint.platform.client_id>${anypoint.platform.client_id}</anypoint.platform.client_id>
                        <anypoint.platform.client_secret>${anypoint.platform.client_secret}</anypoint.platform.client_secret>
                        <skipDeploymentVerification>true</skipDeploymentVerification>
                    </properties>
                </cloudHubDeployment>
            </configuration>
        </plugin>
    </plugins>
</build>
```
