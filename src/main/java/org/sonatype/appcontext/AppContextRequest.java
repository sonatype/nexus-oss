package org.sonatype.appcontext;

import java.util.ArrayList;
import java.util.List;

import org.sonatype.appcontext.internal.Preconditions;
import org.sonatype.appcontext.publisher.EntryPublisher;
import org.sonatype.appcontext.source.EntrySource;

public class AppContextRequest
{
    private final String id;

    private final AppContext parent;

    private final List<EntrySource> sources;

    private final List<EntryPublisher> publishers;

    public AppContextRequest( final String id, final List<EntrySource> sources, final List<EntryPublisher> publishers )
    {
        this( id, null, sources, publishers );
    }

    public AppContextRequest( final String id, final AppContext parent, final List<EntrySource> sources,
                              final List<EntryPublisher> publishers )
    {
        this.id = Preconditions.checkNotNull( id );
        this.parent = parent;
        this.sources = new ArrayList<EntrySource>( Preconditions.checkNotNull( sources ) );
        this.publishers = new ArrayList<EntryPublisher>( Preconditions.checkNotNull( publishers ) );
    }

    public String getId()
    {
        return id;
    }

    public AppContext getParent()
    {
        return parent;
    }

    public List<EntrySource> getSources()
    {
        return sources;
    }

    public List<EntryPublisher> getPublishers()
    {
        return publishers;
    }
}
