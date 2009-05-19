package org.sample.plugin;

import javax.inject.Inject;
import javax.inject.Named;

import org.codehaus.plexus.logging.Logger;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.StorageException;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.storage.local.LocalRepositoryStorage;
import org.sonatype.plexus.appevents.Event;

public class SampleEventInspector
    implements EventInspector
{
    @Inject
    private Logger logger;

    @Inject
    private RepositoryRegistry repositoryRegistry;

    @Inject
    private @Named( "file" )
    LocalRepositoryStorage localRepositoryStorage;

    public boolean accepts( Event<?> evt )
    {
        return true;
    }

    public void inspect( Event<?> evt )
    {
        logger.info( "invoked with event: " + evt.toString() + " with sender " + evt.getEventSender().toString() );

        try
        {
            localRepositoryStorage.isReachable( repositoryRegistry.getRepository( "central" ),
                                                new ResourceStoreRequest( "/" ) );
        }
        catch ( StorageException e )
        {
            logger.error( "Error!", e );
        }
        catch ( NoSuchRepositoryException e )
        {
            logger.warn( "Central is not defined?", e );
        }
    }
}
