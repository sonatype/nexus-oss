package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryEventDownloadRemoteIndexChanged;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "RepositoryDownloadRemoteIndexChangedInspector" )
public class RepositoryDownloadRemoteIndexChangedInspector
    extends AbstractEventInspector
{
    @Requirement
    private NexusScheduler nexusScheduler;
    
    public boolean accepts( Event<?> evt )
    {
        return evt instanceof RepositoryEventDownloadRemoteIndexChanged;
    }

    public void inspect( Event<?> evt )
    {
        RepositoryEventDownloadRemoteIndexChanged event = ( RepositoryEventDownloadRemoteIndexChanged ) evt;
        
        if ( event.getOldValue() == false
            && event.getNewValue() == true )
        {            
            // Create the initial index for the repository
            ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );
            rt.setRepositoryId( event.getRepository().getId() );
            rt.setFullReindex( true );
            nexusScheduler.submit( "Download remote index enabled.", rt );
        }
    }
}
