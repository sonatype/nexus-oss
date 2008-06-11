package org.sonatype.nexus.proxy;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.nexus.proxy.cache.CacheManager;

public class AbstractNexusTestEnvironment
    extends PlexusTestCase
{
    /** The cache manager. */
    private CacheManager cacheManager;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        cacheManager = (CacheManager) lookup( CacheManager.ROLE );

        cacheManager.startService();
    }

    protected void tearDown()
        throws Exception
    {
        cacheManager.stopService();

        super.tearDown();
    }

    /**
     * Gets the cache manager.
     * 
     * @return the cache manager
     */
    protected CacheManager getCacheManager()
    {
        return cacheManager;
    }

}
