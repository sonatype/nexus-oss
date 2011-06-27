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
import org.sonatype.plexus.appevents.Event;

@Named
@Singleton
public class P2MetadataEventsInspector
    implements EventInspector
{

    private final P2RepositoryGenerator p2RepositoryGenerator;

    @Inject
    public P2MetadataEventsInspector( final P2RepositoryGenerator p2RepositoryGenerator )
    {
        this.p2RepositoryGenerator = p2RepositoryGenerator;
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

        return isP2ContentXML( event.getItem() );
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
        p2RepositoryGenerator.updateP2Metadata( event.getItem() );
    }

    private void onItemRemoved( final RepositoryItemEvent event )
    {
        p2RepositoryGenerator.removeP2Metadata( event.getItem() );
    }

    private static boolean isP2ContentXML( final StorageItem item )
    {
        if ( item == null )
        {
            return false;
        }
        return isP2ContentXML( item.getPath() );
    }

    static boolean isP2ContentXML( final String path )
    {
        if ( path == null )
        {
            return false;
        }
        return path.endsWith( "p2Content.xml" );
    }

}
