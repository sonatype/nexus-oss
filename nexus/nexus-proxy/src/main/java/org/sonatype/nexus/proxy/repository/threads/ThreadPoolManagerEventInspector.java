/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions
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
package org.sonatype.nexus.proxy.repository.threads;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.proxy.events.AbstractEventInspector;
import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventAdd;
import org.sonatype.nexus.proxy.events.RepositoryRegistryEventRemove;
import org.sonatype.nexus.proxy.events.RepositoryRegistryRepositoryEvent;
import org.sonatype.plexus.appevents.Event;

/**
 * Maintains the ThreadPoolManager based on Nexus events.
 * 
 * @author cstamas
 */
@Component( role = EventInspector.class, hint = "ThreadPoolManagerEventInspector" )
public class ThreadPoolManagerEventInspector
    extends AbstractEventInspector
{
    @Requirement
    private ThreadPoolManager poolManager;

    @Override
    public boolean accepts( Event<?> evt )
    {
        return evt != null && evt instanceof RepositoryRegistryRepositoryEvent;
        // return evt != null && ( evt instanceof RepositoryRegistryRepositoryEvent || evt instanceof NexusStoppedEvent );
    }

    @Override
    public void inspect( Event<?> evt )
    {
        if ( !accepts( evt ) )
        {
            return;
        }

        if ( evt instanceof RepositoryRegistryEventAdd )
        {
            poolManager.createPool( ( (RepositoryRegistryEventAdd) evt ).getRepository() );

        }
        else if ( evt instanceof RepositoryRegistryEventRemove )
        {
            poolManager.removePool( ( (RepositoryRegistryEventRemove) evt ).getRepository() );
        }
        // else if ( evt instanceof NexusStoppedEvent )
        // {
        // poolManager.shutdown();
        // }
    }
}
