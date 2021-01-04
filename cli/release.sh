#!/bin/bash

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

