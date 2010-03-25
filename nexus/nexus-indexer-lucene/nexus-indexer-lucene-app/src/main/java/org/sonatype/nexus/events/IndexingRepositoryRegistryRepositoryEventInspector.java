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
import org.codehaus.plexus.util.StringUtils;
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
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.plexus.appevents.Event;

/**
 * Listens for events and manages IndexerManager by adding and removing indexing contexts, and doing reindexes when
 * needed (on repository configuration updates).
 * <p>
 * Split {@link RepositoryRegistryRepositoryEventInspector} into two parts. One is this and the other is
 * {@link RepositoryRegistryRepositoryEventInspector} This part is subject to be moved out of core (luceneindexer
 * plugin)
 * 
 * @author Toni Menzel
 */
@Component( role = EventInspector.class, hint = "LuceneIndexingRepositoryRegistryRepositoryEventInspector" )
public class IndexingRepositoryRegistryRepositoryEventInspector
    extends AbstractEventInspector
{
    @Requirement
    private IndexerManager indexerManager;

    @Requirement
    private RepositoryRegistry repoRegistry;

    @Requirement
    private NexusScheduler nexusScheduler;

    private volatile boolean nexusStarted = false;

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
        Repository repository = null;

        if ( evt instanceof NexusStartedEvent )
        {
            nexusStarted = true;

            return;
        }
        else if ( evt instanceof NexusStoppedEvent )
        {
            nexusStarted = false;

            return;
        }
        else if ( evt instanceof RepositoryRegistryRepositoryEvent )
        {
            repository = ( (RepositoryRegistryRepositoryEvent) evt ).getRepository();
        }
        else if ( evt instanceof RepositoryConfigurationUpdatedEvent )
        {
            repository = ( (RepositoryConfigurationUpdatedEvent) evt ).getRepository();
        }
        else
        {
            // how did I get here at all?
            return;
        }

        try
        {
            // check registry for existance, wont be able to do much
            // if doesn't exist yet
            repoRegistry.getRepositoryWithFacet( repository.getId(), MavenRepository.class );

            inspectForIndexerManager( evt, repository );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().debug( "Attempted to handle repository that isn't yet in registry" );
        }
    }

    private void inspectForIndexerManager( Event<?> evt, Repository repository )
    {
        try
        {
            // we are handling repo events, like addition and removal
            if ( evt instanceof RepositoryRegistryEventAdd )
            {
                getIndexerManager().addRepositoryIndexContext( repository.getId() );

                getIndexerManager().setRepositoryIndexContextSearchable( repository.getId(), repository.isSearchable() );

                // create the initial index
                if ( nexusStarted && repository.isIndexable() )
                {
                    // Create the initial index for the repository
                    reindexRepo( repository, true, "Creating initial index, repositoryId=" + repository.getId() );
                }
            }
            else if ( evt instanceof RepositoryRegistryEventRemove )
            {
                getIndexerManager().removeRepositoryIndexContext(
                    ( (RepositoryRegistryEventRemove) evt ).getRepository().getId(), true );
            }
            else if ( evt instanceof RepositoryConfigurationUpdatedEvent )
            {
                getIndexerManager().updateRepositoryIndexContext( repository.getId() );

                if ( evt instanceof RepositoryConfigurationUpdatedEvent )
                {
                    RepositoryConfigurationUpdatedEvent event = (RepositoryConfigurationUpdatedEvent) evt;

                    // we need to do a full reindex of a Maven2 Proxy repository if:
                    // a) if remoteUrl changed
                    // b) if download remote index enabled (any repo type)
                    // c) if repository is made searchable
                    // TODO: are we sure only a) needs a check for Maven2? I think all of them need
                    if ( event.isRemoteUrlChanged() || event.isDownloadRemoteIndexEnabled() || event.isMadeSearchable() )
                    {
                        String taskName = null;

                        String logMessage = null;

                        if ( event.isRemoteUrlChanged() )
                        {
                            taskName = append( taskName, "remote URL changed" );

                            logMessage = append( logMessage, "remote URL changed" );
                        }

                        if ( event.isDownloadRemoteIndexEnabled() )
                        {
                            taskName = append( taskName, "enabled download of indexes" );

                            logMessage = append( logMessage, "enabled download of indexes" );
                        }

                        if ( event.isMadeSearchable() )
                        {
                            taskName = append( taskName, "enabled searchable" );

                            logMessage = append( logMessage, "enabled searchable" );
                        }

                        taskName = taskName + ", repositoryId=" + event.getRepository().getId() + ".";

                        logMessage =
                            logMessage + " on repository \"" + event.getRepository().getName() + "\" (id="
                                + event.getRepository().getId() + "), doing full reindex of it.";

                        reindexRepo( event.getRepository(), true, taskName );

                        getLogger().info( logMessage );
                    }
                }
            }
        }
        catch ( Exception e )
        {
            getLogger().error( "Could not maintain indexing contexts!", e );
        }
    }

    private void reindexRepo( Repository repository, boolean full, String taskName )
    {
        ReindexTask rt = nexusScheduler.createTaskInstance( ReindexTask.class );

        rt.setRepositoryId( repository.getId() );

        rt.setFullReindex( full );

        nexusScheduler.submit( taskName, rt );
    }

    private String append( String message, String append )
    {
        if ( StringUtils.isBlank( message ) )
        {
            return StringUtils.capitalizeFirstLetter( append );
        }
        else
        {
            return message + ", " + append;
        }
    }

}