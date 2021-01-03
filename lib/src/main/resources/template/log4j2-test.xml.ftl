<?xml version="1.0" encoding="UTF-8"?>
<Configuration>
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%-5p %d [%t] %c: %m%n"/>
        </Console>
    </Appenders>

    <Loggers>
    
        <!-- Http Logger shows wire traffic on DEBUG. -->
        <!--AsyncLogger name="org.mule.service.http.impl.service.HttpMessageLogger" level="DEBUG" /-->
        <AsyncLogger name="org.mule.service.http" level="WARN"/>
        <AsyncLogger name="org.mule.extension.http" level="WARN"/>
        
         <!-- Reduce startup noise -->         
        <AsyncLogger name="com.mulesoft.mule.runtime.plugin" level="WARN"/>               
        <AsyncLogger name="org.mule.maven.client" level="WARN"/>
        <AsyncLogger name="org.mule.runtime.core.internal.util" level="WARN"/>
        <AsyncLogger name="org.quartz" level="WARN"/>
        <AsyncLogger name="org.mule.munit.plugins.coverage.server" level="WARN"/>        
               
        <!-- Mule logger -->        
        <AsyncLogger name="org.mule.runtime.core.internal.processor.LoggerMessageProcessor" level="INFO"/>  

        <AsyncRoot level="INFO">
            <AppenderRef ref="Console"/>
        </AsyncRoot>
    </Loggers>

</Configuration>
