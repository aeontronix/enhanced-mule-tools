#!/bin/bash

git clone git@gitlab.com:aeontronix/oss/enhanced-mule-tools.git
cd enhanced-mule-tools
TAG=$(git describe --tags $(git rev-list --tags --max-count=1) | sed 's/^v\(.*\)/\1/')
echo ${TAG}
cd ..
rm -rf enhanced-mule-tools

mkdir -p ../public _tmp _staging
rsync -avz src/ _staging/

jsonschema2md -n -d src/anypoint.schema.json -o _tmp
cat << EOF >_staging/anypoint.schema.markdown
---
layout: page
title: Anypoint Descriptor JSON Schema
permalink: /anypoint-json-descriptor.html
nav_order: 5
---
EOF
cat _tmp/anypoint.schema.md >>_staging/anypoint.schema.markdown
rm -rf _tmp
sed "s/@version@/${TAG}/" src/application-deployment.markdown >_staging/application-deployment.markdown
cd _staging/
bundle install
bundle exec jekyll build -d ../public
