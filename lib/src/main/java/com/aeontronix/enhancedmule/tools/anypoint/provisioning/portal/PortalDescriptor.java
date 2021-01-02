/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.anypoint.provisioning.portal;

import com.aeontronix.commons.FileUtils;
import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.util.EMTLogger;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class PortalDescriptor {
    private static final Logger logger = getLogger(PortalDescriptor.class);
    private static final EMTLogger plogger = new EMTLogger(logger);
    private List<PortalPageDescriptor> pages;

    public List<PortalPageDescriptor> getPages() {
        return pages;
    }

    public void setPages(List<PortalPageDescriptor> pages) {
        this.pages = pages;
    }

    public void provision(ExchangeAsset exchangeAsset) throws IOException {
        if (pages != null) {
            for (PortalPageDescriptor page : pages) {
                String expectedPage;
                if (page.getContent() != null) {
                    expectedPage = page.getContent();
                } else {
                    expectedPage = FileUtils.toString(new File(page.getPath()));
                }
                String pageContent;
                try {
                    pageContent = exchangeAsset.getPage(page.getName());
                } catch (NotFoundException e) {
                    pageContent = null;
                }
                if (pageContent == null || !pageContent.equals(expectedPage)) {
                    exchangeAsset.updatePage(page.getName(), expectedPage);
                    plogger.info(EMTLogger.Product.EXCHANGE, "Updated portal page {} : {}",exchangeAsset.getAssetId(),page.getName());
                }
            }
        }
    }
}
