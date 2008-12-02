package org.sonatype.nexus;

public class SimpleApplicationStatusSource
    implements ApplicationStatusSource
{
    private static SystemStatus systemStatus = new SystemStatus();

    {
        systemStatus.setOperationMode( OperationMode.STANDALONE );
        systemStatus.setState( SystemState.STARTED );
    }

    public SystemStatus getSystemStatus()
    {
        return systemStatus;
    }

    public boolean setState( SystemState state )
    {
        return true;
    }

}
