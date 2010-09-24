package org.sonatype.plexus.components.ehcache;

import net.sf.ehcache.CacheManager;

public interface PlexusEhCacheWrapper
{
    CacheManager getEhCacheManager();
}
