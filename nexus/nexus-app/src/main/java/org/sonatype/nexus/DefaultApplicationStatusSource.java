package org.sonatype.nexus;

import org.codehaus.plexus.component.annotations.Component;

@Component( role = ApplicationStatusSource.class )
public class DefaultApplicationStatusSource
    implements ApplicationStatusSource
{
    /**
     * System status.
     */
    private SystemStatus systemStatus = new SystemStatus();

    public SystemStatus getSystemStatus()
    {
        return systemStatus;
    }

    public boolean setState( SystemState state )
    {
        systemStatus.setState( state );

        return true;
    }
}
