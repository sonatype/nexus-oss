package org.sonatype.nexus;

import org.sonatype.nexus.configuration.ApplicationConfiguration;
import org.sonatype.nexus.configuration.ConfigurationChangeEvent;

/**
 * Event fired after Nexus is started.
 * 
 * @author cstamas
 */
public class NexusStartedEvent
    extends ConfigurationChangeEvent
{
    public NexusStartedEvent( ApplicationConfiguration applicationConfiguration )
    {
        super( applicationConfiguration );
    }
}
