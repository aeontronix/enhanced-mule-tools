#!/bin/bash

set -e

cd "$(cd -P -- "$(dirname -- "$0")" && pwd -P)"

VERSION=$1
if [[ "$VERSION" == "" ]]; then
  VERSION=${POM_REL_VERSION}
  if [[ "$VERSION" == "" ]]; then
    VERSION=$(cd .. && mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
  fi
fi

echo Building EMule Docs v${VERSION}

rm -rf target/pages _staging/
mkdir -p ../target _staging/refdocs/schemas
# Generate schema docs
jsonschema2md -n -d ../lib/src/main/resources/ -o _staging/refdocs/schemas -h false
for f in _staging/refdocs/schemas/*.md; do
  cat > "$f.tmp" << EOF
---
layout: default
title: "@title@"
parent: JSON Schemas
grand_parent: Reference Docs
nav_order: 100
nav_exclude: true
---
EOF
  cat >> "$f.tmp" $f
  mv -f "$f.tmp" $f
  sed -i.bak -e 's/\.md/.html/' $f
done
sed -i.bak -e 's/@title@/Application Provisioning/' -e 's/100/1/' -e 's/exclude: true/exclude: false/' _staging/refdocs/schemas/emule-application.md
rsync -av src/ _staging/
find _staging -name "*.markdown" -exec sed -i.bak "s/@version@/${VERSION}/" {} +
find . -name "*.bak" -exec rm -f {} \;
cp ../lib/src/main/resources/*.schema.json _staging/refdocs/schemas/
cd _staging/
bundle install
bundle exec jekyll build -d ../target/pages
cd ../target/pages
zip -r ../../target/docs.zip *
