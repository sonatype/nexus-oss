package org.sonatype.security;

import java.util.Collection;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.realm.Realm;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

/**
 * Wrapper around a Shiro SecurityManager, it provides a way to start/stop the SecurityManager as well as get/set the configured realms.
 * @author Brian Demers
 *
 */
public interface PlexusSecurityManager
    extends SecurityManager
{
    /**
     * Starts the PlexusSecurityManager.  Before this method is called the state is unknown.
     * @throws StartingException
     */
    void start()
        throws StartingException;

    /**
     * Stops the PlexusSecurityManager.  Provides a way to clean up resources.
     * @throws StoppingException
     */
    void stop()
        throws StoppingException;
    
    void setRealms( Collection<Realm> realms );
    
    Collection<Realm> getRealms();
}
