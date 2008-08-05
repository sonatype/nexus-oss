package org.sonatype.nexus;

import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

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
