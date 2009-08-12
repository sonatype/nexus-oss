package org.sonatype.nexus.events;

import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.maven.AbstractMavenRepository;
import org.sonatype.nexus.proxy.repository.AbstractProxyRepository;
import org.sonatype.nexus.proxy.repository.ConfigurableRepository;
import org.sonatype.nexus.proxy.repository.Repository;
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
            
            // Only need to handle one or the other changed, not both, otherwise
            // will expire cache twice
            if ( changes.containsKey( ConfigurableRepository.CONFIG_LOCAL_URL ) )
            {
                localUrlChanged( event.getRepository() );
            }
            else if ( changes.containsKey( AbstractProxyRepository.CONFIG_REMOTE_URL ) )
            {
                remoteUrlChanged( event.getRepository() );
            }
            
            if ( changes.containsKey( AbstractMavenRepository.CONFIG_DOWNLOAD_REMOTE_INDEX ) )
            {
                downloadRemoteIndexesChanged( event.getRepository(), ( Boolean ) changes.get( AbstractMavenRepository.CONFIG_DOWNLOAD_REMOTE_INDEX  ) );
            }
        }
    }
    
    protected void downloadRemoteIndexesChanged( Repository repository, Boolean downloadIndexes )
    {
        if ( downloadIndexes.equals( Boolean.TRUE ) )
        {
            // Create the initial index for the repository
            ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );
            rt.setRepositoryId( repository.getId() );
            rt.setFullReindex( true );
            nexusScheduler.submit( "Download remote index enabled.", rt );
        }
    }
    
    protected void localUrlChanged( Repository repository )
    {
        getLogger().info(
            "The local url of repository '" + repository.getId()
                + "' has been changed, now expire its caches." );

        ExpireCacheTask task = nexusScheduler.createTaskInstance( ExpireCacheTask.class );

        task.setRepositoryId( repository.getId() );

        nexusScheduler.submit( "Expire caches for repository '" + repository.getId() + "'.", task );        
    }
    
    protected void remoteUrlChanged( Repository repository )
    {
        getLogger().info(
            "The remote url of repository '" + repository.getId()
                + "' has been changed, now expire its caches." );

        ExpireCacheTask task = nexusScheduler.createTaskInstance( ExpireCacheTask.class );

        task.setRepositoryId( repository.getId() );

        nexusScheduler.submit( "Expire caches for repository '" + repository.getId() + "'.", task );
    }
}
