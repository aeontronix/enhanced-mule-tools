#/bin/sh

MVN_CMD="@MVN_CMD@"
MVN_OPTS="@MVN_OPTS@"

${MVN_CMD} ${MVN_OPTS} -Danypoint.deploy.file=exchange://@GROUP_ID@:@ARTIFACT_ID@:@VERSION@ com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin:@EMT_VERSION@:deploy $@
