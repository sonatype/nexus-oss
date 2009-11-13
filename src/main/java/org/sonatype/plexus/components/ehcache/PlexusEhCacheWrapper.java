package org.sonatype.plexus.components.ehcache;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

import net.sf.ehcache.CacheManager;

public interface PlexusEhCacheWrapper
{
    CacheManager getEhCacheManager();

    void start()
        throws StartingException;

    void stop()
        throws StoppingException;
}
