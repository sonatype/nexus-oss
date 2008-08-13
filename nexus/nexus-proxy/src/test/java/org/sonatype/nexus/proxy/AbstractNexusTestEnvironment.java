package org.sonatype.nexus.proxy;

import org.codehaus.plexus.util.FileUtils;
import org.sonatype.nexus.proxy.cache.CacheManager;
import org.sonatype.nexus.proxy.item.RepositoryItemUidFactory;

public abstract class AbstractNexusTestEnvironment
    extends AbstractNexusTestCase
{
    /** The cache manager. */
    private CacheManager cacheManager;

    private RepositoryItemUidFactory repositoryItemUidFactory;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        
        FileUtils.deleteDirectory( PLEXUS_HOME );
        
        PLEXUS_HOME.mkdirs();
        WORK_HOME.mkdirs();
        CONF_HOME.mkdirs();

        cacheManager = (CacheManager) lookup( CacheManager.ROLE );

        cacheManager.startService();

        repositoryItemUidFactory = (RepositoryItemUidFactory) lookup( RepositoryItemUidFactory.class );
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

    protected RepositoryItemUidFactory getRepositoryItemUidFactory()
    {
        return repositoryItemUidFactory;
    }

}
