package org.sonatype.nexus;

public interface ApplicationStatusSource
{
    SystemStatus getSystemStatus();

    boolean setState( SystemState state );
}
