package org.sonatype.nexus.events;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.plexus.appevents.Event;

/**
 * Catches Nexus shutdown event and cleanly stops the IndexManager
 * 
 * @author bdemers
 */
@Component( role = EventInspector.class, hint = "IndexerNexusStoppedEventInspector" )
public class IndexerNexusStoppedEventInspector
    extends AbstractEventInspector
{

    @Requirement
    private IndexerManager indexerManager;

    protected IndexerManager getIndexerManager()
    {
        return indexerManager;
    }

    public boolean accepts( Event<?> evt )
    {
        // listen for STORE, CACHE, DELETE only
        return ( NexusStoppedEvent.class.isAssignableFrom( evt.getClass() ) );
    }

    public void inspect( Event<?> evt )
    {
        try
        {
            indexerManager.shutdown( false );
        }
        catch ( IOException e )
        {
            getLogger().error( "Error while stopping IndexerManager:", e );
        }
    }
}
