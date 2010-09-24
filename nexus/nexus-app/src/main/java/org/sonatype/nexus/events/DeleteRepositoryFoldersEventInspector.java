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
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.DeleteRepositoryFoldersTask;
import org.sonatype.plexus.appevents.Event;

/**
 * Spawns a background task to delete repository folders upon removal.
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "DeleteRepositoryFoldersEventInspector" )
public class DeleteRepositoryFoldersEventInspector
    extends AbstractEventInspector
{
    @Requirement
    private RepositoryRegistry repoRegistry;

    @Requirement
    private NexusScheduler nexusScheduler;

    public boolean accepts( Event<?> evt )
    {
        return ( evt instanceof RepositoryRegistryEventRemove );
    }

    public void inspect( Event<?> evt )
    {
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

            try
            {
                if ( evt instanceof RepositoryRegistryEventRemove )
                {
                    // remove the storage folders for the repository
                    DeleteRepositoryFoldersTask task =
                        nexusScheduler.createTaskInstance( DeleteRepositoryFoldersTask.class );

                    task.setRepository( repository );

                    nexusScheduler.submit( "Deleting repository folder for repository \"" + repository.getName()
                        + "\" (id=" + repository.getId() + ").", task );
                }

            }
            catch ( Exception e )
            {
                getLogger().error(
                    "Could not remove repository folders for repository \"" + repository.getName() + "\" (id="
                        + repository.getId() + ")!", e );
            }
        }
        catch ( NoSuchRepositoryException e )
        {
            getLogger().debug( "Attempted to handle repository that isn't yet in registry" );
        }
    }
}
