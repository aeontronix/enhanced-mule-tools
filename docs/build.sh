#!/bin/bash

set -e

VERSION=$1

rm -rf _build/ _staging/
mkdir -p ../_build _staging/schemas
rsync -avz src/ _staging/
jsonschema2md -n -d ../lib/src/main/resources/ -o _staging/schemas -h false
find _staging -depth -name "*.md" -exec sh -c 'mv "$1" "${1%.md}.markdown"' _ {} \;
find _staging -name "*.markdown" -exec sed -i.bak "s/@version@/${VERSION}/" {} +
find . -name "*.bak" -exec rm -f {} \;
cd _staging/
bundle install
bundle exec jekyll build -d ../_build

