/*
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.maven.wl.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.maven.wl.WLConfig;
import org.sonatype.nexus.util.SystemPropertiesHelper;

/**
 * Default implementation. For now it uses mostly system properties for things like scrape depth and similar, while
 * constants are used for noscrape flag path and locally published prefix file. Latter will probably remain constant (do
 * we want users to change this?), while former should be modified in very rare cases, so unsure do we need real
 * "configiuration" or this here is enough.
 * 
 * @author cstamas
 * @since 2.4
 */
@Named
@Singleton
public class WLConfigImpl
    implements WLConfig
{
    private static final String NO_SCRAPE_FLAG_PATH = "/.meta/noscrape.txt";

    private static final String LOCAL_PREFIX_FILE_PATH = "/.meta/prefixes.txt";

    private static final String[] REMOTE_PREFIX_FILE_PATHS = SystemPropertiesHelper.getStringlist(
        WLConfig.class.getName() + ".prefixFilePaths", "/.meta/prefixes.txt", "/.meta/prefixes.txt.gz" );

    private static final int SCRAPE_DEPTH = SystemPropertiesHelper.getInteger( WLConfig.class.getName()
        + ".scrapeDepth", 2 );

    private static final int WL_MATCHING_DEPTH = SystemPropertiesHelper.getInteger( WLConfig.class.getName()
        + ".wlMatchingDepth", SCRAPE_DEPTH );

    @Override
    public String getNoScrapeFlagPath()
    {
        return NO_SCRAPE_FLAG_PATH;
    }

    @Override
    public String getLocalPrefixFilePath()
    {
        return LOCAL_PREFIX_FILE_PATH;
    }

    @Override
    public String[] getRemotePrefixFilePaths()
    {
        return REMOTE_PREFIX_FILE_PATHS;
    }

    @Override
    public int getRemoteScrapeDepth()
    {
        return SCRAPE_DEPTH;
    }

    @Override
    public int getLocalScrapeDepth()
    {
        return SCRAPE_DEPTH;
    }

    @Override
    public int getWLMatchingDepth()
    {
        return WL_MATCHING_DEPTH;
    }
}
