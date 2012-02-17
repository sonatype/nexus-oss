package org.sonatype.plexus.components.ehcache;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.sisu.ehcache.CacheManagerComponent;

import net.sf.ehcache.CacheManager;

/**
 * Support to swap-out old Plexus-styled EHCacheManager wrapper.
 * 
 * @author cstamas
 * @deprecated Use directly {@link CacheManagerComponent} instead. This is only to make possible "swap out" of old and
 *             putting in this new component.
 */
@Named( "default" )
@Singleton
@Typed( PlexusEhCacheWrapper.class )
@Deprecated
public class DefaultPlexusEhCacheWrapper
    implements PlexusEhCacheWrapper
{
    private final CacheManagerComponent cacheManagerComponent;

    @Inject
    public DefaultPlexusEhCacheWrapper( final CacheManagerComponent cacheManagerComponent )
    {
        this.cacheManagerComponent = cacheManagerComponent;
    }

    public CacheManager getEhCacheManager()
    {
        return cacheManagerComponent.getCacheManager();
    }

    public void stop()
    {
        // nop
    }
}
