package org.sonatype.nexus.auth;

import org.sonatype.plexus.appevents.AbstractEvent;

public class NexusAuthenticationEvent
    extends AbstractEvent<Object>
{
    private final AuthenticationItem item;
    
    public NexusAuthenticationEvent( Object sender, AuthenticationItem item )
    {
        super( sender );
        this.item = item;
    }
    
    public AuthenticationItem getItem()
    {
        return item;
    }
}
