<?xml version="1.0" encoding="utf-8"?>

<Configuration>
<!-- Use this line to enabled log4j json layout (do same in pom.xml)
<Configuration packages="com.aeontronix.log4j2">
-->
    <!--These are some of the loggers you can enable.
        There are several more you can find in the documentation. 
        Besides this log4j configuration, you can also use Java VM environment variables
        to enable other logs like network (-Djavax.net.debug=ssl or all) and 
        Garbage Collector (-XX:+PrintGC). These will be append to the console, so you will 
        see them in the mule_ee.log file. -->

    <Appenders>
        <RollingFile name="file" fileName="${r"${sys:mule.home}${sys:file.separator}logs${sys:file.separator}${artifactId}.log"}"
                 filePattern="${r"${sys:mule.home}${sys:file.separator}logs${sys:file.separator}${artifactId}-%i.log"}">
            <PatternLayout pattern="%-5p %d [%t] [processor: %X{processorPath}; event: %X{correlationId}] %c: %m%n" />
            <SizeBasedTriggeringPolicy size="10 MB" />
            <DefaultRolloverStrategy max="10"/>
        </RollingFile>
<!-- Uncomment to add json logging file
        <RollingFile name="jsonfile" fileName="${r"${sys:mule.home}${sys:file.separator}logs${sys:file.separator}${artifactId}.log.json"}"
                     filePattern="${r"${sys:mule.home}${sys:file.separator}logs${sys:file.separator}${artifactId}-%i.log.json"}">
            <ELJsonLayout/>
            <SizeBasedTriggeringPolicy size="10 MB" />
            <DefaultRolloverStrategy max="10"/>
        </RollingFile>
-->
    </Appenders>
    <Loggers>
        
        <!-- Http Logger shows wire traffic on DEBUG. -->
        <!--AsyncLogger name="org.mule.service.http.impl.service.HttpMessageLogger" level="DEBUG" /-->
        <AsyncLogger name="org.mule.service.http" level="WARN"/>
        <AsyncLogger name="org.mule.extension.http" level="WARN"/>
    
		<!-- Mule logger -->        
        <AsyncLogger name="org.mule.runtime.core.internal.processor.LoggerMessageProcessor" level="INFO"/>
 
        <AsyncRoot level="INFO">
            <AppenderRef ref="file" />
        </AsyncRoot>
    </Loggers>
</Configuration>
