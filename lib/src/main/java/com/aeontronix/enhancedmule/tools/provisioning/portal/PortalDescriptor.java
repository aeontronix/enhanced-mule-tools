/*
 * Copyright (c) Aeontronix 2020
 */

package com.aeontronix.enhancedmule.tools.provisioning.portal;

import com.aeontronix.enhancedmule.tools.anypoint.NotFoundException;
import com.aeontronix.enhancedmule.tools.anypoint.exchange.ExchangeAsset;
import com.aeontronix.enhancedmule.tools.util.HttpException;
import com.kloudtek.util.FileUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.slf4j.LoggerFactory.getLogger;

public class PortalDescriptor {
    private static final Logger logger = getLogger(PortalDescriptor.class);
    private List<PortalPageDescriptor> pages;

    public List<PortalPageDescriptor> getPages() {
        return pages;
    }

    public void setPages(List<PortalPageDescriptor> pages) {
        this.pages = pages;
    }

    public void provision(ExchangeAsset exchangeAsset) throws IOException {
        if( pages != null ) {
            for (PortalPageDescriptor page : pages) {
                final String expectedPage = FileUtils.toString(new File(page.getPath()));
                String pageContent;
                try {
                    pageContent = exchangeAsset.getPage(page.getName());
                } catch (NotFoundException e) {
                    pageContent = null;
                }
                if( pageContent == null || !pageContent.equals(expectedPage) ) {
                    exchangeAsset.updatePage(page.getName(),expectedPage);
                    logger.info("Updated page "+page.getName());
                }
            }
        }
    }
}
