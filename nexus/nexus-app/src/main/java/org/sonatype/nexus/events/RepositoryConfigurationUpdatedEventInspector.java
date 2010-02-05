package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ExpireCacheTask;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "RepositoryConfigurationUpdatedEventInspector" )
public class RepositoryConfigurationUpdatedEventInspector
    extends AbstractEventInspector
    implements EventInspector
{
    @Requirement
    private NexusScheduler nexusScheduler;

    public boolean accepts( Event<?> evt )
    {
        return evt instanceof RepositoryConfigurationUpdatedEvent || evt instanceof RepositoryGroupMembersChangedEvent;
    }

    public void inspect( Event<?> evt )
    {
        if ( evt instanceof RepositoryConfigurationUpdatedEvent )
        {
            boolean evicting = false;

            RepositoryConfigurationUpdatedEvent event = (RepositoryConfigurationUpdatedEvent) evt;

            if ( event.isLocalUrlChanged() )
            {
                getLogger().info(
                                  "The local url of repository '" + event.getRepository().getId()
                                      + "' has been changed, now expire its caches." );

                ExpireCacheTask task = nexusScheduler.createTaskInstance( ExpireCacheTask.class );

                task.setRepositoryId( event.getRepository().getId() );

                nexusScheduler.submit( "Local URL Changed.", task );

                evicting = true;
            }

            if ( event.isRemoteUrlChanged() )
            {
                if ( !evicting )
                {
                    getLogger().info(
                                      "The remote url of repository '" + event.getRepository().getId()
                                          + "' has been changed, now expire its caches." );

                    ExpireCacheTask task = nexusScheduler.createTaskInstance( ExpireCacheTask.class );

                    task.setRepositoryId( event.getRepository().getId() );

                    nexusScheduler.submit( "Remote URL Changed.", task );

                    evicting = true;
                }

                MavenProxyRepository mavenRepo = event.getRepository().adaptToFacet( MavenProxyRepository.class );
            }
        }
    }
}
