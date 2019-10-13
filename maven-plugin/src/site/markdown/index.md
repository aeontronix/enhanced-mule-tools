# Overview

The anypoint maven plugin is primarily designed to provide support for supporting [automated API management](../api-management-automation.html) when deploying an 
application, but also includes a few extra capabilities in addition to that those.

See [Plugin Documentation](plugin-info.html) for full technical details on installation and usage.

# Deploying project to on-prem (hybrid)

In order to deploy your application to an on-prem server with auto-provisioning, add the following to your pom.xml

```$xml
<plugin>
    <groupId>com.aeontronix.anypoint-tools</groupId>
    <version>1.0-SNAPSHOT</version>
    <artifactId>anypoint-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>hdeploy</id>
            <phase>deploy</phase>
            <goals>
                <goal>hdeploy</goal>
            </goals>
            <configuration>
                <username>[anypointusername]</username>
                <password>[anypointpassword]</password>
                <org>[orgname]</org>
                <env>[envname]</env>
                <target>[target name, can be either cluster name, server group name or server name]</target>
            </configuration>
        </execution>
    </executions>
</plugin>
```

And then just set the following properties:

- anypoint.username : your anypoint username
- anypoint.password : your anypoint password
- anypoint.org : Name of the anypoint organization to deploy to
- anypoint.env : Name of the anypoint environment to deploy to
- anypoint.target : Name of the anypoint target (server/server group/cluster) to deploy to

You will then need to include your API provisioning JSON in your project under src/main/resources/anypoint.json

When that file is present, the deployment will automatically perform the API provisioning.

After the provisioning is done, it will also add/modify a file "apiconfig.properties" in the application being deployed, and 
include the following properties:

- anypoint.api.id : (mule 4 only) The API Id (which is environment-specific)
- anypoint.api.client.id : Client id for the Client Application generated for this API
- anypoint.api.client.secret : Client secret for the Client Application generated for this API
- anypoint.platform.client_id : Environment client id (required by API Autodiscovery)
- anypoint.platform.client_secret : Environment client secret (required by API Autodiscovery)

# Maven Deploy (cloudhub)

Use the following plugin to deploy to cloudhub

```$xml
<plugin>
    <groupId>com.aeontronix.anypoint-tools</groupId>
    <version>[latest version of plugin]</version>
    <artifactId>anypoint-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>deploy</id>
            <phase>deploy</phase>
            <execution>
                <phase>deploy</phase>
                <goals>
                    <goal>cdeploy</goal>
                </goals>
                <configuration>
                    <username>[anypoint username]</username>
                    <password>[anypoint password]</password>
                    <org>[org name]</org>
                    <env>[env name]</env>
                </configuration>
            </execution>
        </execution>
    </executions>
</plugin>
```
