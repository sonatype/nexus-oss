/**
 * ﻿Sonatype Nexus (TM) [Open Source Version].
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at ${thirdpartyurl}.
 *
 * This program is licensed to you under Version 3 only of the GNU General
 * Public License as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * Version 3 for more details.
 *
 * You should have received a copy of the GNU General Public License
 * Version 3 along with this program. If not, see http://www.gnu.org/licenses/.
 */
package org.sonatype.nexus.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.tasks.descriptors.SynchronizeShadowTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ShadowPropertyDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Publish indexes task.
 * 
 * @author cstamas
 */
@Component( role = SchedulerTask.class, hint = SynchronizeShadowTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class SynchronizeShadowsTask
    extends AbstractNexusTask<Object>
{
    public String getShadowRepositoryId()
    {
        return getParameter( ShadowPropertyDescriptor.ID );
    }

    public void setShadowRepositoryId( String shadowRepositoryId )
    {
        getParameters().put( ShadowPropertyDescriptor.ID, shadowRepositoryId );
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        getNexus().synchronizeShadow( getShadowRepositoryId() );

        return null;
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_SYNC_SHADOW_ACTION;
    }

    @Override
    protected String getMessage()
    {
        return "Synchronizing virtual repository ID='" + getShadowRepositoryId() + "') with it's master repository.";
    }

}
