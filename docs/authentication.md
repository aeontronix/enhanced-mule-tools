# Authentication

## Login

In order to perform most operations in Enhanced Mule Tools, you will need to be authenticated, which you can do using
either the `login` CLI command or the maven plugin `login` goal as shown below:

=== "CLI"

    ```
    emt login https://myenhancedmuleserver
    ```

=== "Maven"

    ```
    mvn com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin:{{ emtVersion }}:login -Demt.server.url=https://myenhancedmuleserver

    or if you've already logged in previously using maven settings (see below):

    mvn emt:login -Demt.server.url=https://myenhancedmuleserver
    ```

This will launch a browser (or if it's unable to, use the URL that is provided as a response to the command), which will
display the anypoint login.

You will need to login with your anypoint credentials, and after you have completed authentication you can close your
browser
page or tab.

Please note that the authentication will expire after some time, and you will need to re-login again.

## Maven settings

EMT Login has one additional optional feature: It can automatically set an anypoint bearer token in your maven
settings.xml
file, so that maven can access your anypoint exchange or perform operations like deployments.

In order for this feature to be enabled, it can be either set in the Enhanced Mule Server, or optionally set as a
parameter during login.

This parameter is `--maven-settings` or `-mvn` for the command line, or using the property `emt.maven.settings` using
maven.

ie:

=== "CLI"

    ```
    emt login -mvn my-exchange https://myenhancedmuleserver
    ```

=== "Maven"

    ```
    mvn emt:login -Demt.server.url=https://myenhancedmuleserver -Demt.maven.settings=my-exchange
    ```

This will result in your maven settings.xml file to have a server entry as well as a property added. ie:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>my-exchange</id>
            <username>~~~Token~~~</username>
            <password>43245223-95b6-4912-9554-234243234</password>
        </server>
    </servers>
    <profiles>
        <profile>
            <activation>
                <activeByDefault>true</activeByDefault>
            </activation>
            <id>my-exchange</id>
            <properties>
                <exchange-v2-test>43245223-95b6-4912-9554-234243234</exchange-v2-test>
            </properties>
        </profile>
    </profiles>
</settings>
```

It will automatically use the default path for the settings.xml ( ${user.home}/.m2/settings.xml ), but if you wish to
use
a different file you can override that using `--maven-settings-file` or `-mf` with the CLI or with maven the property
`emt.maven.settings.file`
