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
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.AbstractFeedRecorderEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.HostedRepository;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.ShadowRepository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.DeleteRepositoryFoldersTask;
import org.sonatype.plexus.appevents.Event;

/**
 * Toni:
 * Split {@link RepositoryRegistryRepositoryEventInspector} into two parts.
 * One is this and the other is the extracted indexer logic.
 * This indexer related inspector now sits
 * IndexingRepositoryRegistryRepositoryEventInspector.
 *
 * @author Juven Xu
 */
@Component( role = EventInspector.class, hint = "RepositoryRegistryRepositoryEvent" )
public class RepositoryRegistryRepositoryEventInspector
    extends AbstractFeedRecorderEventInspector
{

    @Requirement
    private RepositoryRegistry repoRegistry;

    @Requirement
    private NexusScheduler nexusScheduler;

    private boolean nexusStarted = false;

    public boolean accepts( Event<?> evt )
    {
        return ( evt instanceof RepositoryRegistryRepositoryEvent )
            || ( evt instanceof RepositoryConfigurationUpdatedEvent ) || ( evt instanceof NexusStartedEvent )
            || ( evt instanceof NexusStoppedEvent );
    }

    public void inspect( Event<?> evt )
    {
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

        Repository repository = null;

        if ( evt instanceof RepositoryRegistryRepositoryEvent )
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

            inspectForNexus( evt, repository );

            inspectForIndexerManager( evt, repository );
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().debug( "Attempted to handle repository that isn't yet in registry" );
        }
    }

    private void inspectForNexus( Event<?> evt, Repository repository )
    {
        // we do not want RSS entries about boot and repo additions during boot
        if ( nexusStarted )
        {
            StringBuffer sb = new StringBuffer();

            if ( repository.getRepositoryKind().isFacetAvailable( GroupRepository.class ) )
            {
                sb.append( " repository group " );
            }
            else
            {
                sb.append( " repository " );
            }

            sb.append( repository.getName() );

            sb.append( " (ID=" );

            sb.append( repository.getId() );

            sb.append( ") " );

            if ( repository.getRepositoryKind().isFacetAvailable( ProxyRepository.class ) )
            {
                sb.append( " as proxy repository for URL " );

                sb.append( repository.adaptToFacet( ProxyRepository.class ).getRemoteUrl() );
            }
            else if ( repository.getRepositoryKind().isFacetAvailable( HostedRepository.class ) )
            {
                sb.append( " as hosted repository" );
            }
            else if ( repository.getRepositoryKind().isFacetAvailable( ShadowRepository.class ) )
            {
                sb.append( " as " );

                sb.append( repository.getClass().getName() );

                sb.append( " virtual repository for " );

                sb.append( repository.adaptToFacet( ShadowRepository.class ).getMasterRepository().getName() );

                sb.append( " (ID=" );

                sb.append( repository.adaptToFacet( ShadowRepository.class ).getMasterRepository().getId() );

                sb.append( ") " );
            }

            sb.append( "." );

            if ( evt instanceof RepositoryRegistryEventAdd )
            {
                sb.insert( 0, "Registered" );
            }
            else if ( evt instanceof RepositoryRegistryEventRemove )
            {
                sb.insert( 0, "Unregistered" );
            }
            else if ( evt instanceof RepositoryConfigurationUpdatedEvent )
            {
                sb.insert( 0, "Updated" );
            }

            getFeedRecorder().addSystemEvent( FeedRecorder.SYSTEM_CONFIG_ACTION, sb.toString() );
        }
    }

    private void inspectForIndexerManager( Event<?> evt, Repository repository )
    {
        try
        {
           if ( evt instanceof RepositoryRegistryEventRemove )
            {
                // remove the storage folders for the repository
                DeleteRepositoryFoldersTask task =
                    nexusScheduler.createTaskInstance( DeleteRepositoryFoldersTask.class );

                task.setRepository( repository );

                nexusScheduler.submit( "Remove repository folder", task );
            }

        }
        catch ( Exception e )
        {
            getLogger().error( "Could not maintain indexing contexts!", e );
        }
    }

}
