# Setup

## Command Line Tool

### Mac (Homebrew)

You can install EMT via homebrew by using the following commands:

```shell
brew tap aeontronix/enhancedmule
brew install emt
```

to install a milestone release (ie alpha, beta, rc releases), instead use emt-milestone:

```
brew tap aeontronix/enhancedmule
brew install emt-milestone
```

### Windows

A windows .exe executable version of emt can
be [downloaded here](https://repo1.maven.org/maven2/com/aeontronix/enhanced-mule/enhanced-mule-tools-cli/{{emtVersion}}/enhanced-mule-tools-cli-{{emtVersion}}.exe)

### Others

A binary zip archive can
be [downloaded here](https://repo1.maven.org/maven2/com/aeontronix/enhanced-mule/enhanced-mule-tools-cli/{{emtVersion}}/enhanced-mule-tools-cli-{{emtVersion}}-dist.zip)

A binary tbz2 archive can
be [downloaded here](https://repo1.maven.org/maven2/com/aeontronix/enhanced-mule/enhanced-mule-tools-cli/{{emtVersion}}/enhanced-mule-tools-cli-{{emtVersion}}-dist.tbz2)

## Maven ( In mule applications )

In order to add use enhanced mule tools to deploy your application, you should make the following changes to your
pom.xml:

- Add the enhanced-mule-tools-maven-plugin
- Set property `maven.deploy.skip` to true
- Disable the mule plugin `exchange-mule-maven-plugin` (unfortunately this can't be done via property file at this time,
  so you need to declare it in your pom and set the `skip` property to true)

```xml
<project>
    <properties>
        <maven.deploy.skip>true</maven.deploy.skip>
    </properties>
    <build>
        <plugins>
            <plugin>
                <groupId>org.mule.tools.maven</groupId>
                <artifactId>exchange-mule-maven-plugin</artifactId>
                <version>0.0.17</version>
                <configuration>
                    <skip>true</skip>
                </configuration>
            </plugin>
            <plugin>
                <groupId>com.aeontronix.enhanced-mule</groupId>
                <artifactId>enhanced-mule-tools-maven-plugin</artifactId>
                <version>1.3.0-rc7-SNAPSHOT</version>
                <extensions>true</extensions>
                <executions>
                    <execution>
                        <goals>
                            <goal>process-descriptor</goal>
                            <goal>deploy</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```
