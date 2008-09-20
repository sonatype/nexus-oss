package org.sonatype.nexus.jsecurity.realms;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.jsecurity.realms.MethodRealm;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.jsecurity.NexusSecurity;

public abstract class AbstractNexusRealm
    extends MethodRealm
        implements NexusRealm,
        Initializable
{
    /**
     * @plexus.requirement
     */
    private NexusSecurity security;
    
    @Override
    public void initialize()
        throws InitializationException
    {
        super.initialize();
        
        security.addConfigurationChangeListener( this );
    }
    
    public void onConfigurationChange( ConfigurationChangeEvent evt )
    {
        this.clearCache();
    }
}
