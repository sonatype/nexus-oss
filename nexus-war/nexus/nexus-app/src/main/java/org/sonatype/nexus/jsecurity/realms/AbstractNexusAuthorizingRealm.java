package org.sonatype.nexus.jsecurity.realms;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.jsecurity.realms.XmlMethodAuthorizingRealm;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.jsecurity.NexusSecurity;
import org.sonatype.nexus.proxy.events.AbstractEvent;

public abstract class AbstractNexusAuthorizingRealm
    extends XmlMethodAuthorizingRealm
    implements NexusAuthorizingRealm, Initializable
{
    @Requirement
    private NexusSecurity security;

    public void initialize()
        throws InitializationException
    {
        security.addProximityEventListener( this );
    }

    public void onProximityEvent( AbstractEvent evt )
    {
        if ( ConfigurationChangeEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            if ( getAuthorizationCache() != null )
            {
                getAuthorizationCache().clear();
            }
            if ( getConfigurationManager() != null )
            {
                getConfigurationManager().clearCache();
            }
        }
    }
}
