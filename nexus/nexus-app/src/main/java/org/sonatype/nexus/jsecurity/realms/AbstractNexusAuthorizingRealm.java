package org.sonatype.nexus.jsecurity.realms;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.jsecurity.realms.XmlMethodAuthorizingRealm;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.jsecurity.NexusSecurity;

public abstract class AbstractNexusAuthorizingRealm
    extends XmlMethodAuthorizingRealm
        implements NexusAuthorizingRealm,
        Initializable
{
    /**
     * @plexus.requirement
     */
    private NexusSecurity security;
    
    public void initialize()
        throws InitializationException
    {
        security.addConfigurationChangeListener( this );
    }
    
    public void onConfigurationChange( ConfigurationChangeEvent evt )
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
