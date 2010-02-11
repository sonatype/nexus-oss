package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.plexus.appevents.Event;

@Component( role = EventInspector.class, hint = "RepositoryConfigurationUpdatedEventInspector" )
public class RepositoryConfigurationUpdatedIndexEventInspector
    extends AbstractEventInspector
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
            boolean indexing = false;

            RepositoryConfigurationUpdatedEvent event = (RepositoryConfigurationUpdatedEvent) evt;

            if ( event.isRemoteUrlChanged() )
            {
                MavenProxyRepository mavenRepo = event.getRepository().adaptToFacet( MavenProxyRepository.class );

                if ( mavenRepo != null && mavenRepo.isDownloadRemoteIndexes() )
                {
                    getLogger().info(
                                      "The remote url of repository '" + event.getRepository().getId()
                                          + "' has been changed, now reindex the repository." );

                    // Create the initial index for the repository
                    ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );
                    rt.setRepositoryId( event.getRepository().getId() );
                    rt.setFullReindex( true );
                    nexusScheduler.submit( "Remote URL Changed.", rt );

                    indexing = true;
                }
            }

            if ( event.isDownloadRemoteIndexEnabled() )
            {
                if ( !indexing )
                {
                    getLogger().info(
                                      "The download remote index flag of repository '" + event.getRepository().getId()
                                          + "' has been changed, now reindex the repository." );

                    // Create the initial index for the repository
                    ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );
                    rt.setRepositoryId( event.getRepository().getId() );
                    rt.setFullReindex( true );
                    nexusScheduler.submit( "Download remote index enabled.", rt );
                    indexing = true;
                }
            }

            if ( event.isMadeSearchable() )
            {
                if ( !indexing )
                {
                    getLogger().info(
                                      "The repository '" + event.getRepository().getId()
                                          + "' is made searchable, now reindex the repository." );

                    // Create the initial index for the repository
                    ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );
                    rt.setRepositoryId( event.getRepository().getId() );
                    rt.setFullReindex( true );
                    nexusScheduler.submit( "Searchable enabled (repository \"" + event.getRepository().getName()
                        + "\").", rt );
                    indexing = true;
                }
            }
        }
    }
}
