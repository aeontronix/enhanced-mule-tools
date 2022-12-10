/*
 * Copyright (c) Aeontronix 2022
 */

package com.aeontronix.enhancedmule.tools.application;

import com.aeontronix.commons.file.FileUtils;
import com.aeontronix.commons.file.TempDir;
import com.aeontronix.commons.io.IOUtils;
import com.aeontronix.restclient.RESTClient;
import com.aeontronix.restclient.RESTException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationSourceEnhancerTest {
    private RESTClient restClient;

    @BeforeEach
    void setUp() throws RESTException {
        restClient = Mockito.mock(RESTClient.class);
        final HashMap<Object, Object> r1 = new HashMap<>();
        r1.put("tag_name", "v1.5");
        Mockito.when(restClient.get("https://gitlab.com/api/v4/projects/14801271/releases", List.class)).thenReturn(Collections.singletonList(r1));
        final HashMap<Object, Object> r2 = new HashMap<>();
        r2.put("tag_name", "v1.0");
        Mockito.when(restClient.get("https://gitlab.com/api/v4/projects/39986379/releases", List.class)).thenReturn(Collections.singletonList(r2));
    }

    @Test
    public void testApplicationSourceEnhancerUpdate() throws Exception {
        try (TempDir dir = new TempDir("test")) {
            final File pomFile = new File(dir, "pom.xml");
            FileUtils.write(pomFile, IOUtils.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("/pom-with-emule.xml"))));
            new ApplicationSourceEnhancer(restClient, dir).execute();
            final String modifiedPom = FileUtils.toString(pomFile);
            System.out.println(modifiedPom);
            assertEquals("<?xmlversion=\"1.0\"encoding=\"UTF-8\"?><projectxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0https://maven.apache.org/xsd/maven-4.0.0.xsd\"><modelVersion>4.0.0</modelVersion><groupId>com.mycompany</groupId><artifactId>testproject</artifactId><version>1.0.0-SNAPSHOT</version><packaging>mule-application</packaging><name>testproject</name><properties><project.build.sourceEncoding>UTF-8</project.build.sourceEncoding><project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding><app.runtime>4.3.0-20210322</app.runtime><mule.maven.plugin.version>3.5.1</mule.maven.plugin.version></properties><build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-clean-plugin</artifactId><version>3.0.0</version></plugin><plugin><groupId>org.mule.tools.maven</groupId><artifactId>mule-maven-plugin</artifactId><version>${mule.maven.plugin.version}</version><extensions>true</extensions></plugin><plugin><groupId>org.mule.tools.maven</groupId><artifactId>exchange-mule-maven-plugin</artifactId><version>0.0.17</version><configuration><skip>true</skip></configuration></plugin><plugin><groupId>com.aeontronix.enhanced-mule</groupId><artifactId>enhanced-mule-tools-maven-plugin</artifactId><version>1.5</version><extensions>true</extensions><executions><execution><goals><goal>process-descriptor</goal><goal>deploy</goal></goals></execution></executions></plugin></plugins></build><dependencies><dependency><groupId>org.mule.connectors</groupId><artifactId>mule-http-connector</artifactId><version>1.5.24</version><classifier>mule-plugin</classifier></dependency><dependency><groupId>org.mule.connectors</groupId><artifactId>mule-sockets-connector</artifactId><version>1.2.1</version><classifier>mule-plugin</classifier></dependency></dependencies><repositories><repository><id>anypoint-exchange-v2</id><name>AnypointExchange</name><url>https://maven.anypoint.mulesoft.com/api/v2/maven</url><layout>default</layout></repository><repository><id>mulesoft-releases</id><name>MuleSoftReleasesRepository</name><url>https://repository.mulesoft.org/releases/</url><layout>default</layout></repository></repositories><pluginRepositories><pluginRepository><id>mulesoft-releases</id><name>MuleSoftReleasesRepository</name><layout>default</layout><url>https://repository.mulesoft.org/releases/</url><snapshots><enabled>false</enabled></snapshots></pluginRepository></pluginRepositories></project>",
                    modifiedPom.replaceAll("\\s", ""));
        }
    }

    @Test
    public void testApplicationSourceEnhancerNew() throws Exception {
        try (TempDir dir = new TempDir("test")) {
            final File pomFile = new File(dir, "pom.xml");
            FileUtils.write(pomFile, IOUtils.toByteArray(Objects.requireNonNull(getClass().getResourceAsStream("/project/minimal/pom.xml"))));
            new ApplicationSourceEnhancer(restClient, dir).execute();
            final String modifiedPom = FileUtils.toString(pomFile);
            System.out.println(modifiedPom);
            assertEquals("<?xmlversion=\"1.0\"encoding=\"UTF-8\"?><projectxmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0https://maven.apache.org/xsd/maven-4.0.0.xsd\"><modelVersion>4.0.0</modelVersion><groupId>com.mycompany</groupId><artifactId>testproject</artifactId><version>1.0.0-SNAPSHOT</version><packaging>mule-application</packaging><name>testproject</name><properties><project.build.sourceEncoding>UTF-8</project.build.sourceEncoding><project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding><app.runtime>4.3.0-20210322</app.runtime><mule.maven.plugin.version>3.5.1</mule.maven.plugin.version></properties><build><plugins><plugin><groupId>org.apache.maven.plugins</groupId><artifactId>maven-clean-plugin</artifactId><version>3.0.0</version></plugin><plugin><groupId>org.mule.tools.maven</groupId><artifactId>mule-maven-plugin</artifactId><version>${mule.maven.plugin.version}</version><extensions>true</extensions></plugin><plugin><groupId>com.aeontronix.enhanced-mule</groupId><artifactId>enhanced-mule-tools-maven-plugin</artifactId><version>1.5</version><executions><execution><goals><goal>process-descriptor</goal><goal>deploy</goal></goals></execution></executions></plugin></plugins></build><dependencies><dependency><groupId>org.mule.connectors</groupId><artifactId>mule-http-connector</artifactId><version>1.5.24</version><classifier>mule-plugin</classifier></dependency><dependency><groupId>org.mule.connectors</groupId><artifactId>mule-sockets-connector</artifactId><version>1.2.1</version><classifier>mule-plugin</classifier></dependency></dependencies><repositories><repository><id>anypoint-exchange-v2</id><name>AnypointExchange</name><url>https://maven.anypoint.mulesoft.com/api/v2/maven</url><layout>default</layout></repository><repository><id>mulesoft-releases</id><name>MuleSoftReleasesRepository</name><url>https://repository.mulesoft.org/releases/</url><layout>default</layout></repository></repositories><pluginRepositories><pluginRepository><id>mulesoft-releases</id><name>MuleSoftReleasesRepository</name><layout>default</layout><url>https://repository.mulesoft.org/releases/</url><snapshots><enabled>false</enabled></snapshots></pluginRepository></pluginRepositories></project>",
                    modifiedPom.replaceAll("\\s", ""));
        }
    }
}
