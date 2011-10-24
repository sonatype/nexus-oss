package org.sonatype.nexus.maven.tasks;

import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryEventExpireCaches;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.scheduling.TaskUtil;

/**
 * TODO
 *
 * @author: cstamas
 */
public class Nexus4588CancellationEventInspector
    implements EventInspector
{

    private boolean active;

    public boolean isActive()
    {
        return active;
    }

    public void setActive( final boolean active )
    {
        this.active = active;
    }

    @Override
    public boolean accepts( final Event<?> evt )
    {
        return isActive() && evt instanceof RepositoryEventExpireCaches;
    }

    @Override
    public void inspect( final Event<?> evt )
    {
        if ( isActive() )
        {
            TaskUtil.getCurrentProgressListener().cancel();
        }
    }
}
