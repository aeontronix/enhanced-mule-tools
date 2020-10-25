#!/bin/bash

sh build.sh
aws s3 sync public/ s3://docs.enhanced-mule.com.s3-us-west-2.amazonaws.com/
