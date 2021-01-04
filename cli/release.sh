#!/bin/bash

set -e

source /usr/bin/prepare-build

FILE=formula/emt.rb

RELVERSION=${POM_REL_VERSION}
DIST_SHA=($(shasum -a 256 cli/target/enhanced-mule-tools-cli-${RELVERSION}.jar))

sed -e "s/@version@/${RELVERSION}/g" -e "s/@checksum@/${DIST_SHA}/" cli/src/main/assembly/emt.tap.rb >cli/target/emt.rb

CONTENT=$(openssl base64 -A -in cli/target/emt.rb)

URL=https://api.github.com/repos/enhanced-mule/homebrew-tools/contents/${FILE}

SHA=$(curl -s -X GET ${URL} | jq -r .sha)

JSON="{\"path\": \"${FILE}\", \"message\": \"update\", \"content\": \"${CONTENT}\", \"branch\": \"master\", \"sha\": \"${SHA}\" }"

curl -s --request PUT \
  --url ${URL} \
  --header 'Accept: application/vnd.github.v3+json' \
  --header "Authorization: token ${GITHUB_TOKEN}" \
  --header 'Content-Type: application/json' \
  --data "${JSON}"

#echo Creating gitlab release
#
#if [[ "${POM_REL_VERSION}" =~ .*"beta".* ]] || [[ "${POM_REL_VERSION}" =~ .*"alpha".* ]]; then
#  echo "Skipping GL release for beta/alpha release version: ${POM_REL_VERSION}"
#else
#  echo "Creating gitlab release"
#  curl --header 'Content-Type: application/json' --header "PRIVATE-TOKEN: ${GL_TOKEN}" \
#     --data "{ \"name\": \"v${POM_REL_VERSION}\", \"tag_name\": \"v${POM_REL_VERSION}\", \"description\": \"Release v${POM_REL_VERSION}\", \"assets\": { \"links\": [{ \"name\": \"CLI Distribution (ZIP)\", \"url\": \"https://repo1.maven.org/maven2/com/aeontronix/enhanced-mule/enhanced-mule-tools-cli/${POM_REL_VERSION}/enhanced-mule-tools-cli-${POM_REL_VERSION}dist.zip\" }] } }" \
#     --request POST "https://gitlab.com/api/v4/projects/14801271/releases"
#fi
