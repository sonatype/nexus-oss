package org.sonatype.security;

import java.util.Collection;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
import org.jsecurity.mgt.SecurityManager;
import org.jsecurity.realm.Realm;

public interface PlexusSecurityManager
    extends SecurityManager
{
    void start()
        throws StartingException;
    
    void stop()
        throws StoppingException;
    
    void setRealms( Collection<Realm> realms );
    
    Collection<Realm> getRealms();
}
