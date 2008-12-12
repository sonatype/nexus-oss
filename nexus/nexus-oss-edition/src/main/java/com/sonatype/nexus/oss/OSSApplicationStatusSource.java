package com.sonatype.nexus.oss;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.SystemStatus;

@Component( role = ApplicationStatusSource.class )
public class OSSApplicationStatusSource
    implements ApplicationStatusSource
{
    /**
     * System status.
     */
    private SystemStatus systemStatus = new SystemStatus();
    
    public OSSApplicationStatusSource()
    {
        systemStatus.setEdition( "OSS" );
    }

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
