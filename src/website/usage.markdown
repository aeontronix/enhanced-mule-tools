---
layout: page
title: Usage
permalink: /usage
nav_order: 1
---
# Deploy an application

Enhanced mule tools is a [maven](https://maven.apache.org/) plugin that can be used
either in a mule project to build and deploy a project, or standalone to deploy a pre-compiled 
application jar archive.

In addition to deploying

## Use in a mule project

To use enhanced mule tools in a maven project, add the following plugin to your pom.xml:

```xml
<build>
    <plugins>
        ...
        <plugin>
            <groupId>com.aeontronix.enhanced-mule</groupId>
            <version>@version@</version>
            <artifactId>enhanced-mule-tools-maven-plugin</artifactId>
            <executions>
                <execution>
                    <id>deploy</id>
                    <goals>
                        <goal>prepare-deploy</goal>
                        <goal>deploy</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        ...
    </plugins>
</build>
```

## Use standalone to deploy a pre-compiled jar file

To deploy a pre-compile jar file ( myapplication.jar in the following example ), use the following 
maven command:

```bash
mvn com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin:@version@:deploy -Dfile=myapplication.jar
```

# Proxy

If you need to use a proxy, set your proxy settings in settings.xml as [described here](https://maven.apache.org/guides/mini/guide-proxies.html)

## `deploy` goal parameters

The deploy goal of the maven plugin supports the following parameters for both pom-based and standalone usage.

- Name: name of the parameter when settings it in the `<configuration>` section of the pom.xml
- Property: name of the property used to set the parameter (this is the only way to set parameters when using the standalone invocation)
- Description: Description of the parameter
- Required: If the parameter is required

|| Name || Property || Required || Description
| org | anypoint.org | Yes | Organization Name
| env | anypoint.env | Yes | Environment Name
| skipApiProvisioning | anypoint.api.provisioning.skip | No | If set to `true`, automatic API Manager provisioning will be skipped
| skipDeploy | anypoint.deploy.skip | No |If set to `true`, the deployment will be skipped
| filename | anypoint.deploy.filename | No | Filename (if not specified the file's name will be used)
| appName | anypoint.deploy.name | No | Application name (if not specified will use filename )
| force | anypoint.deploy.force | No | If true, will force deployment even if same already application was already deployed.
| skipWait | anypoint.deploy.skipwait | No | If true will skip wait for application to start
| deployTimeout | anypoint.deploy.timeout | No | Deployment timeout in milliseconds ( defaults to 10 min)
| deployRetryDelay | anypoint.deploy.retrydelay | No | Delay (in milliseconds) in retrying a deployment ( defaults to 2.5 seconds )
| properties | | No | Application Properties that will be set in Runtime Manager (note this must be in the form of a [maven properties](https://maven.apache.org/pom.html#Properties) ) 
| fileProperties | | No | Properties that will be injected in a property file in the project's archive at deploy time (rather than in Runtime Manager)
| filePropertiesPath | | No | Path of the property file (inside the application jar) for properties set in `fileProperties`
| filePropertiesSecure | | No | If set to true, all secure properties will be moved into `fileProperties` rather than `properties`
| vars | | No | Variables for anypoint.json templating |

When running standalone, the following parameters are also supported

|| Name || Property || Required || Description
| file | anypoint.deploy.file | Yes | File to deploy | Standalone |

When deploying to Cloudhub, the following parameters are also supported:

|| Name || Property || Required || Description

When deploying to on-prem / Hybrid, the following parameters are also supported:

|| Name || Property || Required || Description
| target | anypoint.target | Yes | Anypoint target name (Server / Server Group / Cluster)
