package org.sonatype.nexus;

import org.sonatype.nexus.configuration.ConfigurationChangeEvent;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;

/**
 * Event fired before Nexus is being shutdown (before any shutdown action is taken).
 * 
 * @author cstamas
 */
public class NexusStoppingEvent
    extends ConfigurationChangeEvent
{
    public NexusStoppingEvent( ApplicationConfiguration applicationConfiguration )
    {
        super( applicationConfiguration );
    }
}
