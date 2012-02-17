package org.sonatype.plexus.components.ehcache;

import net.sf.ehcache.CacheManager;

import org.sonatype.sisu.ehcache.CacheManagerComponent;

/**
 * Support to swap-out old Plexus-styled EHCacheManager wrapper.
 * 
 * @author cstamas
 * @deprecated Use directly {@link CacheManagerComponent} instead. This is only to make possible "swap out" of old and
 *             putting in this new component.
 */
public interface PlexusEhCacheWrapper
{
    CacheManager getEhCacheManager();
    
    void stop();
}
