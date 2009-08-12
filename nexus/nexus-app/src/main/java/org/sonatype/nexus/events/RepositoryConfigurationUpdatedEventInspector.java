package org.sonatype.nexus.events;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepository;
import org.sonatype.nexus.proxy.repository.ConfigurableRepository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ExpireCacheTask;
import org.sonatype.nexus.tasks.ReindexTask;
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
        return evt instanceof RepositoryConfigurationUpdatedEvent;
    }

    public void inspect( Event<?> evt )
    {
        if ( evt instanceof RepositoryConfigurationUpdatedEvent )
        {
            RepositoryConfigurationUpdatedEvent event = ( RepositoryConfigurationUpdatedEvent ) evt;
            
            Map<String,Object> changes = event.getChanges();
            
            boolean evicting = false;
            boolean indexing = false;
            
            if ( changes.containsKey( ConfigurableRepository.CONFIG_LOCAL_URL ) )
            {
                getLogger().info(
                    "The local url of repository '" + event.getRepository().getId()
                        + "' has been changed, now expire its caches." );
                
                ExpireCacheTask task = nexusScheduler.createTaskInstance( ExpireCacheTask.class );

                task.setRepositoryId( event.getRepository().getId() );
                
                nexusScheduler.submit( "Local URL Changed.", task );
                
                evicting = true;
            }

            if ( changes.containsKey( AbstractProxyRepository.CONFIG_REMOTE_URL ) )
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
                
                if ( event.getRepository().adaptToFacet( MavenProxyRepository.class ).isDownloadRemoteIndexes() )
                {
                    getLogger().info(
                        "The remote url of repository '" + event.getRepository().getId()
                            + "' has been changed, now reindex the repository." );
                    
                    // Create the initial index for the repository
                    ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );
                    rt.setRepositoryId( event.getRepository().getId() );
                    rt.setFullReindex( true );
                    nexusScheduler.submit( "Remote URL Changed.", rt );
                }
                
                indexing = true;
            }
            
            if ( changes.containsKey( AbstractMavenRepository.CONFIG_DOWNLOAD_REMOTE_INDEX ) )
            {
                if ( !indexing && ( Boolean ) changes.get( AbstractMavenRepository.CONFIG_DOWNLOAD_REMOTE_INDEX  ).equals( Boolean.TRUE ) )
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
        }
    }
}
