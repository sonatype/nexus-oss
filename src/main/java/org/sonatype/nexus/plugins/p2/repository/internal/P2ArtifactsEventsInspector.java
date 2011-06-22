package org.sonatype.nexus.plugins.p2.repository.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.p2.repository.P2RepositoryGenerator;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryItemEvent;
import org.sonatype.nexus.proxy.events.RepositoryItemEventCache;
import org.sonatype.nexus.proxy.events.RepositoryItemEventDelete;
import org.sonatype.nexus.proxy.events.RepositoryItemEventStore;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.plexus.appevents.Event;

@Named
@Singleton
public class P2ArtifactsEventsInspector
    implements EventInspector
{

    private final P2RepositoryGenerator p2RepositoryGenerator;

    private final RepositoryRegistry repositories;

    @Inject
    public P2ArtifactsEventsInspector( final P2RepositoryGenerator p2RepositoryGenerator,
                                       final RepositoryRegistry repositories )
    {
        this.p2RepositoryGenerator = p2RepositoryGenerator;
        this.repositories = repositories;
    }

    @Override
    public boolean accepts( final Event<?> evt )
    {
        if ( evt == null
            || !( evt instanceof RepositoryItemEvent )
            || !( evt instanceof RepositoryItemEventStore || evt instanceof RepositoryItemEventCache || evt instanceof RepositoryItemEventDelete ) )
        {
            return false;
        }

        final RepositoryItemEvent event = (RepositoryItemEvent) evt;

        return isP2Artifacts( event.getItem() );
    }

    @Override
    public void inspect( final Event<?> evt )
    {
        if ( !accepts( evt ) )
        {
            return;
        }

        final RepositoryItemEvent event = (RepositoryItemEvent) evt;

        if ( event instanceof RepositoryItemEventStore || event instanceof RepositoryItemEventCache )
        {
            onItemAdded( event );
        }
        else if ( event instanceof RepositoryItemEventDelete )
        {
            onItemRemoved( event );
        }
    }

    private void onItemAdded( final RepositoryItemEvent event )
    {
        p2RepositoryGenerator.updateP2Artifacts( event.getItem() );
    }

    private void onItemRemoved( final RepositoryItemEvent event )
    {
        p2RepositoryGenerator.removeP2Artifacts( event.getItem() );
    }

    private static boolean isP2Artifacts( final StorageItem item )
    {
        if ( item == null )
        {
            return false;
        }
        final String path = item.getPath();
        if ( path == null )
        {
            return false;
        }
        return path.endsWith( "p2Artifacts.xml" );
    }

}
