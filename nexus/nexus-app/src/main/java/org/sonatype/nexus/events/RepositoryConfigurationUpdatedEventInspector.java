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
import org.sonatype.nexus.proxy.events.RepositoryConfigurationUpdatedEvent;
import org.sonatype.nexus.scheduling.NexusScheduler;
import org.sonatype.nexus.tasks.ExpireCacheTask;
import org.sonatype.plexus.appevents.Event;

/**
 * Event inspector listening for configuration changes to expire caches when Local or Remote URL changed of the
 * repository.
 */
@Component( role = EventInspector.class, hint = "RepositoryConfigurationUpdatedEventInspector" )
public class RepositoryConfigurationUpdatedEventInspector
    extends AbstractEventInspector
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
            RepositoryConfigurationUpdatedEvent event = (RepositoryConfigurationUpdatedEvent) evt;

            if ( event.isLocalUrlChanged() || event.isRemoteUrlChanged() )
            {
                String taskName = null;
                String logMessage = null;

                if ( event.isLocalUrlChanged() && event.isRemoteUrlChanged() )
                {
                    // both changed
                    taskName = "Local and Remote URLs changed, repositoryId=" + event.getRepository().getId() + ".";

                    logMessage =
                        "The Local and Remote URL of repository \"" + event.getRepository().getName() + "\" (id="
                            + event.getRepository().getId() + ") has been changed, expiring its caches.";

                }
                else if ( !event.isLocalUrlChanged() && event.isRemoteUrlChanged() )
                {
                    // remote URL changed
                    taskName = "Remote URL changed, repositoryId=" + event.getRepository().getId() + ".";

                    logMessage =
                        "The Remote URL of repository \"" + event.getRepository().getName() + "\" (id="
                            + event.getRepository().getId() + ") has been changed, expiring its caches.";
                }
                else if ( event.isLocalUrlChanged() && !event.isRemoteUrlChanged() )
                {
                    // local URL changed
                    taskName = "Local URL changed, repositoryId=" + event.getRepository().getId() + ".";

                    logMessage =
                        "The Local URL of repository \"" + event.getRepository().getName() + "\" (id="
                            + event.getRepository().getId() + ") has been changed, expiring its caches.";
                }

                ExpireCacheTask task = nexusScheduler.createTaskInstance( ExpireCacheTask.class );

                task.setRepositoryId( event.getRepository().getId() );

                nexusScheduler.submit( taskName, task );

                getLogger().info( logMessage );
            }
        }
    }
}
