package org.sonatype.plexus.components.ehcache;

import net.sf.ehcache.CacheManager;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

public interface PlexusEhCacheWrapper
{
    
    CacheManager getEhCacheManager();
    
    public void start()
    throws StartingException;
    
    public void stop()
    throws StoppingException;
}
