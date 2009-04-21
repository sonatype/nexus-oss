package org.sonatype.security.events;

public interface SecurityEventHandler
{

    void handleEvent( SecurityEvent event );
    
}
