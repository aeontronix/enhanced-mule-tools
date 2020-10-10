---
layout: page
title: Mule Application Deployment
permalink: /application-deployment.html
nav_order: 1
---

# Overview

Enhanced mule tools can be used to deploy an application that is built from source code, 
or a pre-compiled application jar, through the maven goal 'deploy'

# Use in a mule project

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

# Use standalone to deploy a pre-compiled jar file

To deploy a pre-compile jar file ( myapplication.jar in the following example ), use the following 
maven command:

```bash
mvn com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin:@version@:deploy -Dfile=myapplication.jar
```

# Proxy Settings

If you need to use a proxy, set your proxy settings in settings.xml as [described here](https://maven.apache.org/guides/mini/guide-proxies.html)

# Parameters

For all deploy parameters see [Maven Plugin Docs](maven-plugin.html#deploy)
