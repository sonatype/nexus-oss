package org.sonatype.nexus.proxy.maven.wl.internal;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.proxy.maven.wl.WLConfig;
import org.sonatype.nexus.util.SystemPropertiesHelper;

/**
 * Default implementation.
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
