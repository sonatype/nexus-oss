package org.sonatype.plexus.components.ehcache;

import net.sf.ehcache.CacheManager;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;

public interface PlexusEhCacheWrapper extends Startable
{
    
    CacheManager getEhCacheManager();
}
