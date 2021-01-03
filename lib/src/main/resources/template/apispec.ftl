<#-- @formatter:off -->
<#if apiSpecType == 'raml'>
#%RAML 1.0
title: foo-raml
version: 1.0.0
<#elseif apiSpecType == 'oas2json'>
{
    "swagger": "2.0",
    "info": {
        "version": "1.0.0",
        "title": "${artifactId}"
    },
    "paths": {}
}
<#elseif apiSpecType == 'oas2yaml'>
swagger: "2.0"
info:
    version: 1.0.0
    title: foo-yml
paths: {}
<#elseif apiSpecType == 'oas3json'>
{
    "openapi": "3.0.0",
    "info": {
        "version": "1.0.0",
        "title": "oas3json"
    },
    "paths": {}
}
<#elseif apiSpecType == 'oas3yaml'>
openapi: "3.0.0"
info:
    version: 1.0.0
    title: foo-yml
paths: {}
</#if>
<#-- @formatter:on -->
