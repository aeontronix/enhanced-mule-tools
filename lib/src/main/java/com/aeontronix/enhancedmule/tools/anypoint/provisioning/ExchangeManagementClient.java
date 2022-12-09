/*
 * Copyright (c) Aeontronix 2021
 */

package com.aeontronix.enhancedmule.tools.anypoint.provisioning;

import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.Organization;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetProvisioningException;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.AssetVersion;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAssetDescriptor;
import com.aeontronix.enhancedmule.tools.legacy.deploy.ApplicationSource;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

public class ExchangeManagementClient {
    private static final Logger logger = getLogger(ExchangeManagementClient.class);
    private static final EMTLogger plogger = new EMTLogger(logger);
    public static final String SNAPSHOT_SUFFIX = "-snapshot";
    public static final int SNAPSHOT_SUFFIX_LEN = SNAPSHOT_SUFFIX.length();

    public boolean publish(ExchangeAssetDescriptor asset, Organization organization, ApplicationSource source,
                           ProvisioningRequest provisioningRequest) throws AssetProvisioningException {
        return asset.publish(organization, source, provisioningRequest);
    }

    public void deleteSnapshots(Organization organization, ExchangeAssetDescriptor asset) throws HttpException {
        String version = asset.getVersion();
        String exclude;
        final int snapIdx = version.toLowerCase().indexOf(SNAPSHOT_SUFFIX);
        if(snapIdx != -1) {
            exclude = version;
            version = version.substring(0,snapIdx+SNAPSHOT_SUFFIX_LEN);
        } else {
            exclude = null;
            version = version + "-snapshot";
        }
        try {
            final ExchangeAsset exchangeAsset = organization.findExchangeAsset(organization.getId(), asset.getId());
            for (AssetVersion exchangeAssetVersion : exchangeAsset.getVersions()) {
                final String v = exchangeAssetVersion.getVersion();
                if( v.toLowerCase().startsWith(version.toLowerCase()) ) {
                    if( exclude == null || !exclude.equals(v) ) {
                        exchangeAssetVersion.delete();
                        plogger.info(EMTLogger.Product.EXCHANGE, "Deleted snapshot exchange asset: {} : {}",asset.getId(), v);
                    }
                }
            }
        } catch (NotFoundException e) {
            //
        }
    }
}
