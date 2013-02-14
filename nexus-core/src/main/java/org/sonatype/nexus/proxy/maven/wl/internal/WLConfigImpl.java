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

import java.util.ArrayList;
import java.util.List;

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
    /**
     * System property key that is used to read up boolean value controlling is WL feature active or not. Main use case
     * is to disable this in "legacy" UTs and ITs, but might serve too as troubleshooting in some cases. Event
     * dispatcher is active by default, to deactivate it, specify a system property like this:
     * 
     * <pre>
     * org.sonatype.nexus.proxy.maven.wl.WLConfig.featureActive = false
     * </pre>
     * 
     * Note: This does NOT REMOVE the Feature itself! The feature will be still present and working but remote content
     * discovery will be completely disabled, hence, all the proxies and groups having proxies as members will simply be
     * marked for noscrape. Also, since no WL will be published, no "proxy optimization" will happen either. If neglect
     * the noscape, Nexus will work as it was working before 2.4 release. Using system property with this key should be
     * restricted to tests or some troubleshooting only.
     */
    public static final String FEATURE_ACTIVE_KEY = WLConfig.class.getName() + ".featureActive";

    private static final String LOCAL_NO_SCRAPE_FLAG_PATH = "/.meta/noscrape.txt";

    private static final String LOCAL_PREFIX_FILE_PATH = "/.meta/prefixes.txt";

    private static final String[] EXTRA_REMOTE_NO_SCRAPE_FLAG_PATHS =
        SystemPropertiesHelper.getStringlist( WLConfig.class.getName() + ".extraRemoteNoscrapeFlagPaths" );

    private static final String[] EXTRA_REMOTE_PREFIX_FILE_PATHS =
        SystemPropertiesHelper.getStringlist( WLConfig.class.getName() + ".extraRemotePrefixFilePaths" );

    private static final int REMOTE_SCRAPE_DEPTH = SystemPropertiesHelper.getInteger( WLConfig.class.getName()
        + ".remoteScrapeDepth", 2 );

    private static final int LOCAL_SCRAPE_DEPTH = SystemPropertiesHelper.getInteger( WLConfig.class.getName()
        + ".localScrapeDepth", 2 );

    private static final int PREFIX_FILE_MAX_ENTRY_COUNT = SystemPropertiesHelper.getInteger( WLConfig.class.getName()
        + ".prefixFileMaxEntryCount", 10000 );

    private final boolean featureActive;

    /**
     * Default constructor.
     */
    public WLConfigImpl()
    {
        this( SystemPropertiesHelper.getBoolean( FEATURE_ACTIVE_KEY, true ) );
    }

    /**
     * Constructor.
     * 
     * @param featureActive
     */
    public WLConfigImpl( final boolean featureActive )
    {
        this.featureActive = featureActive;
    }

    @Override
    public boolean isFeatureActive()
    {
        return featureActive;
    }

    @Override
    public String getLocalNoScrapeFlagPath()
    {
        return LOCAL_NO_SCRAPE_FLAG_PATH;
    }

    @Override
    public String getLocalPrefixFilePath()
    {
        return LOCAL_PREFIX_FILE_PATH;
    }

    @Override
    public List<String> getRemoteNoScrapeFlagPaths()
    {
        final ArrayList<String> result = new ArrayList<String>();
        result.add( LOCAL_NO_SCRAPE_FLAG_PATH );
        for ( String extra : EXTRA_REMOTE_NO_SCRAPE_FLAG_PATHS )
        {
            if ( extra != null && extra.trim().length() > 0 )
            {
                result.add( extra );
            }
        }
        return result;
    }

    @Override
    public List<String> getRemotePrefixFilePaths()
    {
        final ArrayList<String> result = new ArrayList<String>();
        result.add( LOCAL_PREFIX_FILE_PATH );
        for ( String extra : EXTRA_REMOTE_PREFIX_FILE_PATHS )
        {
            if ( extra != null && extra.trim().length() > 0 )
            {
                result.add( extra );
            }
        }
        return result;
    }

    @Override
    public int getRemoteScrapeDepth()
    {
        return REMOTE_SCRAPE_DEPTH;
    }

    @Override
    public int getLocalScrapeDepth()
    {
        return LOCAL_SCRAPE_DEPTH;
    }

    @Override
    public int getPrefixFileMaxEntriesCount()
    {
        return PREFIX_FILE_MAX_ENTRY_COUNT;
    }
}
