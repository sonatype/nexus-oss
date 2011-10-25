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
package org.sonatype.nexus.maven.tasks;

import org.sonatype.nexus.proxy.events.EventInspector;
import org.sonatype.nexus.proxy.events.RepositoryEventExpireCaches;
import org.sonatype.nexus.proxy.events.RepositoryItemEventRetrieve;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.scheduling.TaskUtil;

/**
 * Cancellation inspector that cancels the current thread, simulating user intervention about cancelling tasks.
 * It will cancel whenever a repository subject expires caches. This relies on implementation detail that
 * snapshot remover upon 1st pass will perform cache expiration.
 *
 * @author: cstamas
 */
public class Nexus4588CancellationEventInspector
    implements EventInspector
{

    private boolean active;

    public boolean isActive()
    {
        return active;
    }

    public void setActive( final boolean active )
    {
        this.active = active;
    }

    @Override
    public boolean accepts( final Event<?> evt )
    {
        return isActive() && evt instanceof RepositoryEventExpireCaches;
    }

    @Override
    public void inspect( final Event<?> evt )
    {
        if ( isActive() )
        {
            TaskUtil.getCurrentProgressListener().cancel();
        }
    }
}
