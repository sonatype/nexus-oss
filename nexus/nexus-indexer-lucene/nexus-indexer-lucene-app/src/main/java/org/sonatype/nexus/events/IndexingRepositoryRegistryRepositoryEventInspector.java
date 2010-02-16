/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.nexus.proxy.maven.MavenProxyRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.plexus.appevents.Event;

/**
 * Split {@link RepositoryRegistryRepositoryEventInspector} into two parts.
 * One is this and the other is {@link RepositoryRegistryRepositoryEventInspector}
 *
 * This part is subject to be moved out of core (luceneindexer plugin)
 *
 * @author Toni Menzel
 */
@Component( role = EventInspector.class, hint = "LuceneIndexingRepositoryRegistryRepositoryEventInspector" )
public class IndexingRepositoryRegistryRepositoryEventInspector extends AbstractEventInspector
{

    @Requirement
    private IndexerManager indexerManager;

    @Requirement
    private RepositoryRegistry repoRegistry;

    @Requirement
    private NexusScheduler nexusScheduler;

    private boolean nexusStarted = false;

    protected IndexerManager getIndexerManager()
    {
        return indexerManager;
    }

    public boolean accepts( Event<?> evt )
    {
        return ( evt instanceof RepositoryRegistryRepositoryEvent )
               || ( evt instanceof RepositoryConfigurationUpdatedEvent ) || ( evt instanceof NexusStartedEvent )
               || ( evt instanceof NexusStoppedEvent );
    }

    public void inspect( Event<?> evt )
    {
        if( evt instanceof NexusStartedEvent )
        {
            nexusStarted = true;

            return;
        }
        else if( evt instanceof NexusStoppedEvent )
        {
            nexusStarted = false;

            return;
        }

        Repository repository = null;

        if( evt instanceof RepositoryRegistryRepositoryEvent )
        {
            repository = ( (RepositoryRegistryRepositoryEvent) evt ).getRepository();
        }
        else
        {
            repository = ( (RepositoryConfigurationUpdatedEvent) evt ).getRepository();
        }

        try
        {
            // check registry for existance, wont be able to do much
            // if doesn't exist yet
            repoRegistry.getRepository( repository.getId() );

            inspectForIndexerManager( evt, repository );
        }
        catch( NoSuchRepositoryException e )
        {
            getLogger().debug( "Attempted to handle repository that isn't yet in registry" );
        }
    }

    private void inspectForIndexerManager( Event<?> evt, Repository repository )
    {
        try
        {
            // we are handling repo events, like addition and removal
            if( evt instanceof RepositoryRegistryEventAdd )
            {
                getIndexerManager().addRepositoryIndexContext( repository.getId() );

                getIndexerManager().setRepositoryIndexContextSearchable( repository.getId(), repository.isSearchable() );

                // create the initial index
                if( nexusStarted && repository.isIndexable() )
                {
                    // Create the initial index for the repository
                    reindexRepo( repository );
                }
            }
            else if ( evt instanceof RepositoryRegistryEventRemove )
            {
                getIndexerManager().removeRepositoryIndexContext(
                    ( (RepositoryRegistryEventRemove) evt ).getRepository().getId(),
                    true );
            }
            else if ( evt instanceof RepositoryConfigurationUpdatedEvent )
            {
                getIndexerManager().updateRepositoryIndexContext( repository.getId() );
                
                RepositoryConfigurationUpdatedEvent event = (RepositoryConfigurationUpdatedEvent) evt;
                
                MavenProxyRepository mavenRepo = repository.adaptToFacet( MavenProxyRepository.class );

                if ( event.isRemoteUrlChanged() 
                    && mavenRepo != null && mavenRepo.isDownloadRemoteIndexes() )
                {
                    getLogger().info(
                                      "The remote url of repository '" + event.getRepository().getId()
                                          + "' has been changed, now reindex the repository." );
                    
                    reindexRepo( repository );
                }
                else if ( event.isDownloadRemoteIndexEnabled() )
                {
                    getLogger().info(
                                      "The download remote index flag of repository '" + event.getRepository().getId()
                                          + "' has been changed, now reindex the repository." );
                 
                    reindexRepo( repository );
                }
                else if ( event.isMadeSearchable() )
                {
                    getLogger().info(
                                      "The repository '" + event.getRepository().getId()
                                          + "' is made searchable, now reindex the repository." );
                    
                    reindexRepo( repository );
                }
            }
        }
        catch( Exception e )
        {
            getLogger().error( "Could not maintain indexing contexts!", e );
        }
    }
    
    private void reindexRepo( Repository repository )
    {
        ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );

        if( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
        {
            rt.setRepositoryGroupId( repository.getId() );
        }
        else
        {
            rt.setRepositoryId( repository.getId() );
        }
        nexusScheduler.submit( "Create initial index.", rt );
    }
}