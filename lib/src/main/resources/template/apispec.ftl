<#-- @formatter:off -->
<#if apiSpecType == 'raml'>
#%RAML 1.0
title: ${artifactId}
version: ${apiSpecVersion}
<#elseif apiSpecType == 'oas2json'>
{
    "swagger": "2.0",
    "info": {
        "version": "${apiSpecVersion}",
        "title": "${artifactId}"
    },
    "paths": {}
}
<#elseif apiSpecType == 'oas2yaml'>
swagger: "2.0"
info:
    version: ${apiSpecVersion}
    title: ${artifactId}
paths: {}
<#elseif apiSpecType == 'oas3json'>
{
    "openapi": "3.0.0",
    "info": {
        "version": "${apiSpecVersion}",
        "title": "oas3json"
    },
    "paths": {}
}
<#elseif apiSpecType == 'oas3yaml'>
openapi: "3.0.0"
info:
    version: ${apiSpecVersion}
    title: ${artifactId}
paths: {}
</#if>
<#-- @formatter:on -->
