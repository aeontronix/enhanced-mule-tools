#!/bin/bash

function upd() {
  sed "s/emt.version>.*</emt.version>${VERSION}-SNAPSHOT</" examples/$1/pom.xml >examples/$1/pom.xml_
  mv examples/$1/pom.xml_ examples/$1/pom.xml
}

export POM_VERSION=$( xmlstarlet sel -N 'p=http://maven.apache.org/POM/4.0.0' -t -v '/p:project/p:version/text()' pom.xml | sed 's/-SNAPSHOT$//' );
echo "Latest tag: $(git describe --abbrev=0 --tags)"
echo "Current version: ${POM_VERSION}"
echo "New version ?"
read VERSION
mvn versions:set -DprocessAllModules=true -DgroupId='*' -DartifactId='*' -DnewVersion=${VERSION}-SNAPSHOT
upd provision-only
upd provision-and-deploy
upd provision-and-deploy-inprojectraml
upd provision-and-deploy-to-exchange
upd provision-and-deploy-compat
upd publish-rest
