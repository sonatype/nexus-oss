package org.sonatype.security;

import java.util.Collection;

import org.apache.shiro.realm.Realm;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

public interface PlexusSecurityManager
    extends org.apache.shiro.mgt.SecurityManager
{
    void start()
        throws StartingException;
    
    void stop()
        throws StoppingException;
    
    void setRealms( Collection<Realm> realms );
    
    Collection<Realm> getRealms();
}
