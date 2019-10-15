#!/bin/bash

export POM_VERSION=$( xmlstarlet sel -N 'p=http://maven.apache.org/POM/4.0.0' -t -v '/p:project/p:version/text()' pom.xml | sed 's/-SNAPSHOT$//' );
echo "Latest tag: $(git describe --abbrev=0 --tags)"
echo "Current version: ${POM_VERSION}"
echo "New version ?"
read VERSION
mvn versions:set -DprocessAllModules=true -DgroupId='*' -DartifactId='*' -DnewVersion=${VERSION}-SNAPSHOT
