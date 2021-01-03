<#assign emp = emProperties == 'true'>
<#assign restProject = projectType == 'rest'>
<#assign d = domain == 'true'>
{
<#if restProject>
  "api": {
  }<#if emp>,</#if>
</#if>
<#if emp>
  "properties": {
<#if restProject && !d >
    "listener.http": {
      "type": "https",
      "secure": true
    }
</#if>
  }
</#if>
}
