<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">
	<modelVersion>4.0.0</modelVersion>

	<groupId>${groupId}</groupId>
	<artifactId>${artifactId}</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>mule-application</packaging>

	<name>${artifactId}</name>
    <#if description?has_content><description>${description}</description></#if>

    <properties>
		<project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
		<project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <maven.deploy.skip>true</maven.deploy.skip>
		<app.runtime>${muleRuntimeVersion}-${muleRuntimeSubVersion}</app.runtime>
		<mule.maven.plugin.version>${muleMavenPluginVersion}</mule.maven.plugin.version>
        <emt.version>${emtVersion}</emt.version>
	</properties>

	<build>
		<plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-clean-plugin</artifactId>
                <version>3.1.0</version>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-site-plugin</artifactId>
                <version>3.9.1</version>
            </plugin>
			<plugin>
				<groupId>org.mule.tools.maven</groupId>
				<artifactId>mule-maven-plugin</artifactId>
				<version>${r"${mule.maven.plugin.version}"}</version>
				<extensions>true</extensions>
				<configuration>
				</configuration>
			</plugin>
            <plugin>
                <groupId>com.aeontronix.enhanced-mule</groupId>
                <artifactId>enhanced-mule-tools-maven-plugin</artifactId>
                <version>${r"${emt.version}"}</version>
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

	<dependencies>
<#if domain == 'true'>
        <dependency>
            <groupId>${domainGroupId}</groupId>
            <artifactId>${domainArtifactId}</artifactId>
            <version>${domainVersion}</version>
            <classifier>mule-domain</classifier>
            <scope>provided</scope>
        </dependency>
</#if>
<#if projectType == 'rest' && domain != 'true' >
        <dependency>
            <groupId>org.mule.connectors</groupId>
            <artifactId>mule-http-connector</artifactId>
            <version>1.5.22</version>
            <classifier>mule-plugin</classifier>
        </dependency>
        <dependency>
            <groupId>org.mule.connectors</groupId>
            <artifactId>mule-sockets-connector</artifactId>
            <version>1.2.0</version>
            <classifier>mule-plugin</classifier>
        </dependency>
        <dependency>
            <groupId>org.mule.modules</groupId>
            <artifactId>mule-apikit-module</artifactId>
            <version>1.3.12</version>
            <classifier>mule-plugin</classifier>
        </dependency>
</#if>
<#if emProperties != 'true'>
    <!-- Uncomment to enable Enhanced Mule Properties provider
         Which you REALLY should do as it will make you life much easier
         see for details: https://gitlab.com/aeontronix/oss/enhanced-mule/enhanced-mule-properties-provider/-/blob/master/README.md
</#if>
        <dependency>
            <groupId>com.aeontronix.enhanced-mule</groupId>
            <artifactId>enhanced-mule-properties-provider</artifactId>
            <version>${emPropertiesVersion}</version>
            <classifier>mule-plugin</classifier>
        </dependency>
<#if emProperties != 'true'>
    -->
</#if>

<!-- Uncomment to add JSON Log4j Layout
        <dependency>
            <groupId>com.aeontronix.log4j2</groupId>
            <artifactId>log4j2-enhanced-json-layout</artifactId>
            <version>1.0.2</version>
        </dependency>
-->
	</dependencies>

	<repositories>
        <repository>
            <id>anypoint-exchange-v2</id>
            <name>Anypoint Exchange</name>
            <url>https://maven.anypoint.mulesoft.com/api/v2/maven</url>
            <layout>default</layout>
        </repository>
        <repository>
            <id>mulesoft-releases</id>
            <name>MuleSoft Releases Repository</name>
            <url>https://repository.mulesoft.org/releases/</url>
            <layout>default</layout>
        </repository>
    </repositories>
    <pluginRepositories>
        <pluginRepository>
            <id>mulesoft-releases</id>
            <name>mulesoft release repository</name>
            <layout>default</layout>
            <url>https://repository.mulesoft.org/releases/</url>
            <snapshots>
                <enabled>false</enabled>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>

</project>
