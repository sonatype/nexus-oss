package org.sonatype.sisu.ehcache;

import org.sonatype.appcontext.lifecycle.Stoppable;

/**
 * Simple reusable CacheManager component lifecycle handler.
 * 
 * @author cstamas
 * @since 1.1
 */
public class CacheManagerLifecycleHandler
    implements Stoppable
{
    private final CacheManagerComponent cacheManagerComponent;

    public CacheManagerLifecycleHandler( final CacheManagerComponent cacheManagerComponent )
    {
        if ( cacheManagerComponent == null )
        {
            throw new NullPointerException( "Supplied CacheManagerComponent  cannot be null!" );
        }
        this.cacheManagerComponent = cacheManagerComponent;
    }

    public void handle()
    {
        cacheManagerComponent.shutdown();
    }
}
