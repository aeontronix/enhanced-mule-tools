#!/bin/bash

rm -rf src/site/markdown/schema src/site/resources/schema
mkdir -p src/site/markdown/schema src/site/resources/schema
jsonschema2md -d src/schemas -o src/site/markdown/schema
cp src/schemas/* src/site/resources/schema
cp src/schemas/* src/site/markdown/schema
