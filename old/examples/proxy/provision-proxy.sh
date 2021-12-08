#!/usr/bin/env bash

USERNAME=$1
PASSWORD=$2
ENV=$3

mvn com.aeontronix.enhanced-mule:enhanced-mule-tools-maven-plugin:1.1.0-SNAPSHOT:provision -Danypoint.username=$USERNAME \
-Danypoint.password=$PASSWORD -Danypoint.env=$ENV -Danypoint.descriptor=anypoint.json
