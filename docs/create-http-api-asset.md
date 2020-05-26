POST https://anypoint.mulesoft.com/exchange/api/v1/assets

body

```
------WebKitFormBoundaryTmRtRWR3AS5eJN3A
Content-Disposition: form-data; name="organizationId"

d002eff8-84a5-452f-bce6-145f4a688311
------WebKitFormBoundaryTmRtRWR3AS5eJN3A
Content-Disposition: form-data; name="groupId"

d002eff8-84a5-452f-bce6-145f4a688311
------WebKitFormBoundaryTmRtRWR3AS5eJN3A
Content-Disposition: form-data; name="assetId"

sad
------WebKitFormBoundaryTmRtRWR3AS5eJN3A
Content-Disposition: form-data; name="version"

1.0.0
------WebKitFormBoundaryTmRtRWR3AS5eJN3A
Content-Disposition: form-data; name="name"

sad
------WebKitFormBoundaryTmRtRWR3AS5eJN3A
Content-Disposition: form-data; name="classifier"

http
------WebKitFormBoundaryTmRtRWR3AS5eJN3A
Content-Disposition: form-data; name="apiVersion"

v1
------WebKitFormBoundaryTmRtRWR3AS5eJN3A
Content-Disposition: form-data; name="asset"

undefined
------WebKitFormBoundaryTmRtRWR3AS5eJN3A--
```

response 

```json
{"organizationId":"d002eff8-84a5-452f-bce6-145f4a688311","groupId":"d002eff8-84a5-452f-bce6-145f4a688311","assetId":"sad","version":"1.0.0","apiVersion":"v1","versionGroup":"v1","classifier":"http","metadata":{"name":"sad","type":"http-api","classifier":"http","tags":[{"value":"http"},{"value":"api"},{"key":"product-api-version","value":"v1"}]},"name":"sad","type":"http-api"}
```

