package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryGroupMembersChangedEvent;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ExpireCacheTask;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.tasks.ResetGroupIndexTask;
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
        //removed because this is happening far too often
        /*if ( evt instanceof RepositoryGroupMembersChangedEvent )
        {
            GroupRepository repo = ( (RepositoryGroupMembersChangedEvent) evt ).getGroupRepository();

            // Update the repo
            ResetGroupIndexTask rt = nexusScheduler.createTaskInstance( ResetGroupIndexTask.class );
            rt.setRepositoryGroupId( repo.getId() );
            nexusScheduler.submit( "Update group index.", rt );
        }
        else*/ if ( evt instanceof RepositoryConfigurationUpdatedEvent )
        {
            boolean indexing = false;
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
