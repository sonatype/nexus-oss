/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.events;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventPostRemove;
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
    private NexusScheduler nexusScheduler;

    public boolean accepts( Event<?> evt )
    {
        return ( evt instanceof RepositoryRegistryEventPostRemove );
    }

    public void inspect( Event<?> evt )
    {
        Repository repository = ( (RepositoryRegistryEventPostRemove) evt ).getRepository();

        try
        {
            // remove the storage folders for the repository
            DeleteRepositoryFoldersTask task = nexusScheduler.createTaskInstance( DeleteRepositoryFoldersTask.class );

            task.setRepository( repository );

            nexusScheduler.submit( "Deleting repository folder for repository \"" + repository.getName() + "\" (id="
                + repository.getId() + ").", task );
        }
        catch ( Exception e )
        {
            getLogger().error(
                "Could not remove repository folders for repository \"" + repository.getName() + "\" (id="
                    + repository.getId() + ")!", e );
        }
    }
}
