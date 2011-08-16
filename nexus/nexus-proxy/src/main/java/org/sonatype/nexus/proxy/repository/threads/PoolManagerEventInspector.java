package org.sonatype.nexus.proxy.repository.threads;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "PoolManagerEventInspector" )
public class PoolManagerEventInspector
    extends AbstractEventInspector
{
    @Requirement
    private PoolManager poolManager;

    @Override
    public boolean accepts( Event<?> evt )
    {
        return evt instanceof RepositoryRegistryRepositoryEvent;
    }

    @Override
    public void inspect( Event<?> evt )
    {
        if ( evt instanceof RepositoryRegistryEventAdd )
        {
            poolManager.createPool( ( (RepositoryRegistryEventAdd) evt ).getRepository() );

        }
        else if ( evt instanceof RepositoryRegistryEventRemove )
        {
            poolManager.removePool( ( (RepositoryRegistryEventRemove) evt ).getRepository() );
        }
    }
}
