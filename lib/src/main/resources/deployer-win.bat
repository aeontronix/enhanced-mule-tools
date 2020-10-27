if not "%JAVA_HOME%" == "" goto JavaFound
WHERE java
IF %ERRORLEVEL% == 0 goto JavaFound
echo JAVA not found

powershell -noexit "(new-object System.Net.WebClient).DownloadFile('https://github.com/AdoptOpenJDK/openjdk8-binaries/releases/download/jdk8u272-b10/OpenJDK8U-jre_x64_windows_hotspot_8u272b10.zip','OpenJDK8U-jre_x64_windows_hotspot_8u272b10.zip')"
powershell -noexit "Expand-Archive -LiteralPath OpenJDK8U-jre_x64_windows_hotspot_8u272b10.zip -DestinationPath ."
set JAVA_HOME=jdk8u272-b10-jredir jdk8u272-b10-jre
JavaFound:
@MVN_CMD@ @MVN_OPTS@ -Danypoint.deploy.file=exchange://@GROUP_ID@:@ARTIFACT_ID@:@VERSION@ com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin:@EMT_VERSION@:deploy %*
