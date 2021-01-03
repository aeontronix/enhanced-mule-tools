#!/bin/bash

if [[ "${GITHUB_TOKEN}" == "" ]]; then
  echo GITHUB_TOKEN missing
  exit 1
fi

FILE=test.txt

RELVERSION=${POM_VERSION}
DIST_SHA=($(shasum -a 256 cli/target/enhanced-mule-tools-cli-1.3.0-alpha3-SNAPSHOT.jar))

sed -e "s/@version@/${RELVERSION}/g" -e "s/@checksum@/${DIST_SHA}/" cli/src/main/assembly/emt.tap.rb

exit 0


URL=https://api.github.com/repos/enhanced-mule/homebrew-tools/contents/${FILE}

SHA=$(curl -s -X GET ${URL} | jq -r .sha)

echo ${TAP_ENCODED}

JSON="{\"path\": \"${FILE}\", \"message\": \"update\", \"content\": \"cmVxdWlyZSAiZm9ybXVsYSIgY2xhc3MgRW10IDwgRm9ybXVsYSBkZXNjICJFbmhh bmNlZCBNdWxlIENMSSIgaG9tZXBhZ2UgImh0dHBzOi8vd3d3LmVuaGFuY2VkLW11 bGUuY29tLyIgdXJsICJodHRwczovL3JlcG8xLm1hdmVuLm9yZy9tYXZlbjIvY29t L2Flb250cm9uaXgvZW5oYW5jZWQtbXVsZS9lbmhhbmNlZC1tdWxlLXRvb2xzLWNs aS8vZW5oYW5jZWQtbXVsZS10b29scy1jbGktLWRpc3QudGJ6MiIgc2hhMjU2ICJl YTY4YTA5MjEyZDExMzFiMjJiYzFhMjM0M2UwM2E4ODAwODc0NmFhNDMzZjY4ZThi MzkwNDAyOTFjZWU2OWZkIiBoZWFkICJodHRwczovL2dpdGh1Yi5jb20vZW5oYW5j ZWQtbXVsZS9ob21lYnJldy10b29scy5naXQiIGRlZiBpbnN0YWxsIGxpYmV4ZWMu aW5zdGFsbCAiYmluIiwgImxpYiIsICJSRUFETUUubWQiIGJpbi53cml0ZV9leGVj X3NjcmlwdCBsaWJleGVjLyJiaW4vZW10IiBlbmQgdGVzdCBkbyBhc3NlcnRfbWF0 Y2ggIiIsIHNoZWxsX291dHB1dCgiI3tiaW59L2VtdCAtViIsIDIpIGVuZCBlbmQK\", \"branch\": \"master\", \"sha\": \"${SHA}\" }"

echo $JSON

curl --request PUT \
  --url ${URL} \
  --header 'Accept: application/vnd.github.v3+json' \
  --header "Authorization: token ${GITHUB_TOKEN}" \
  --header 'Content-Type: application/json' \
  --data "${JSON}"

