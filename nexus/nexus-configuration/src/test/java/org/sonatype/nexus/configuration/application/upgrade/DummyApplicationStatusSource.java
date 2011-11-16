package org.sonatype.nexus.configuration.application.upgrade;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.ApplicationStatusSource;
import org.sonatype.nexus.SystemState;
import org.sonatype.nexus.SystemStatus;

@Component(role=ApplicationStatusSource.class)
public class DummyApplicationStatusSource
    implements ApplicationStatusSource
{

    @Override
    public SystemStatus getSystemStatus()
    {
        SystemStatus status = new SystemStatus();
        status.setVersion( "1.0" );
        return status;
    }

    @Override
    public boolean setState( SystemState state )
    {
        return false;
    }

}
