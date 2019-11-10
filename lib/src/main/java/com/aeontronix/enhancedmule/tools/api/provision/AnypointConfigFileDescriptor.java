/*
 * Copyright (c) Aeontronix 2019
 */

package com.aeontronix.enhancedmule.tools.api.provision;

import com.aeontronix.enhancedmule.tools.Environment;
import com.kloudtek.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class AnypointConfigFileDescriptor {
    private static final Logger logger = LoggerFactory.getLogger(AnypointConfigFileDescriptor.class);
    private Boolean mule3;
    private APIDescriptor api;
    private HashMap<String,String> armProperties = new HashMap<>();

    public AnypointConfigFileDescriptor() {
    }

    public AnypointConfigFileDescriptor(String name, String version) {
        api = new APIDescriptor(name, version);
    }

    public APIProvisioningResult provision(Environment environment, APIProvisioningConfig config) throws ProvisioningException {
        try {
            APIProvisioningResult result = new APIProvisioningResult();
            if (api != null) {
                logger.debug("API descriptor found, provisioning");
                api.provision(this, environment, config, result);
            }
            return result;
        } catch (Exception e) {
            throw new ProvisioningException(e);
        }

//                // Create client application
//                ClientApplication clientApplication;
//                String clientAppUrl = this.getClientAppUrl();
//                String clientAppDescription = this.getClientAppDescription();
//                if (clientAppDescription == null) {
//                    clientAppDescription = this.getDescription();
//                }
//                try {
//                    logger.debug("Searching for existing client application {}", clientAppName);
//                    clientApplication = org.findClientApplicationByName(clientAppName);
//                    logger.debug("Found existing client application {}: {}", clientAppName, clientApplication.getId());
//                    // TODO: update clientAppUrl & clientAppDescription
//                } catch (NotFoundException e) {
//                    logger.debug("Couldn't find existing client application {}, creating", clientAppName);
//                    clientApplication = org.createClientApplication(clientAppName, clientAppUrl, clientAppDescription);
//                }
//                String credFile = this.getAddCredsToPropertyFile();
//                if (StringUtils.isNotEmpty(credFile)) {
//                    logger.debug("Adding transformer to add credentials to property file: " + credFile);
//                    transformList.add( new SetPropertyTransformer(credFile,this.getCredIdPropertyName(), clientApplication.getClientId()));
//                    transformList.add( new SetPropertyTransformer(credFile,this.getCredSecretPropertyName(), clientApplication.getClientSecret()));
//                }
//                for (ProvisionedAPIAccess access : this.getAccess()) {
//                    String accessVersion = access.getVersion();
//                    if( envSuffix != null ) {
//                        accessVersion = accessVersion +"-"+envSuffix;
//                    }
//                    org.requestAPIAccess(clientApplication, applyVars(access.getName()), applyVars(accessVersion), true, true, null);
//                }
//                for (String name : provisioningConfig.getAccessedBy()) {
//                    org.requestAPIAccess(applyVars(name),applyVars(apiName),applyVars(apiVersionName),true,true,null);
//                }
//            }
//        } catch (ClassCastException e) {
//            throw new IOException("Invalid anypoint.json descriptor", e);
//        }
    }

    public Boolean getMule3() {
        return mule3;
    }

    public void setMule3(Boolean mule3) {
        this.mule3 = mule3;
    }

    public APIDescriptor getApi() {
        return api;
    }

    public void setApi(APIDescriptor api) {
        this.api = api;
    }

    public HashMap<String, String> getArmProperties() {
        return armProperties;
    }

    public void setArmProperties(HashMap<String, String> armProperties) {
        this.armProperties = armProperties;
    }
}
