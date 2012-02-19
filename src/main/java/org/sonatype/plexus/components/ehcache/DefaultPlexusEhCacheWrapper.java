package org.sonatype.plexus.components.ehcache;

import net.sf.ehcache.CacheManager;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.sonatype.sisu.ehcache.CacheManagerComponent;
import org.sonatype.sisu.ehcache.CacheManagerComponentImpl;

/**
 * Support to swap-out old Plexus-styled EHCacheManager wrapper.
 * 
 * @author cstamas
 * @deprecated Use directly {@link CacheManagerComponent} instead. This is only to make possible "swap out" of old and
 *             putting in this new component.
 */
@Deprecated
@Component( role = PlexusEhCacheWrapper.class )
public class DefaultPlexusEhCacheWrapper
    implements PlexusEhCacheWrapper, Disposable
{
    @Requirement
    private CacheManagerComponent cacheManagerComponent;

    public CacheManager getEhCacheManager()
    {
        return cacheManagerComponent.getCacheManager();
    }

    public void stop()
    {
        // nop
    }

    public void dispose()
    {
        if ( cacheManagerComponent instanceof CacheManagerComponentImpl )
        {
            ( (CacheManagerComponentImpl) cacheManagerComponent ).shutdown();
        }
    }
}
