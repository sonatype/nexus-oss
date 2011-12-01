package org.sonatype.nexus.proxy.repository;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.AsynchronousEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.plexus.appevents.Event;

/**
 * A "relaying" event inspector that is made asynchronous and is used to relay the repository content change related
 * events to ShadowRepository instances present in system.
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "ShadowRepositoryEventInspector" )
public class ShadowRepositoryEventInspector
    extends AbstractEventInspector
    implements EventInspector, AsynchronousEventInspector
{
    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Override
    public boolean accepts( Event<?> evt )
    {
        return evt instanceof RepositoryItemEvent;
    }

    @Override
    public void inspect( Event<?> evt )
    {
        if ( evt instanceof RepositoryItemEvent )
        {
            final RepositoryItemEvent ievt = (RepositoryItemEvent) evt;
            final List<ShadowRepository> shadows = repositoryRegistry.getRepositoriesWithFacet( ShadowRepository.class );

            for ( ShadowRepository shadow : shadows )
            {
                if ( shadow.getMasterRepositoryId().equals( ievt.getRepository().getId() ) )
                {
                    shadow.onRepositoryItemEvent( ievt );
                }
            }
        }
    }
}
