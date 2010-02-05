package org.sonatype.nexus.index.events;

import org.sonatype.plexus.appevents.AbstractEvent;

@Deprecated
public class ReindexRepositoriesEvent
extends AbstractEvent<Object>
{

    private ReindexRepositoriesRequest reindexRepositoriesRequest;
    
    public ReindexRepositoriesEvent( Object component, ReindexRepositoriesRequest reindexRepositoriesRequest )
    {
        super( component );
        this.reindexRepositoriesRequest = reindexRepositoriesRequest;
    }

    public ReindexRepositoriesRequest getReindexRepositoriesRequest()
    {
        return reindexRepositoriesRequest;
    }
}
