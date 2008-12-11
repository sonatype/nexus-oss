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
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;
import org.sonatype.nexus.tasks.descriptors.RebuildAttributesTaskDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Rebuild attributes task.
 * 
 * @author cstamas
 */
@Component( role = SchedulerTask.class, hint = RebuildAttributesTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class RebuildAttributesTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
{    
    public Object doRun()
        throws Exception
    {
        if ( getRepositoryGroupId() != null )
        {
            getNexus().rebuildAttributesRepositoryGroup( getResourceStorePath(), getRepositoryGroupId() );
        }
        else if ( getRepositoryId() != null )
        {
            getNexus().rebuildAttributesRepository( getResourceStorePath(), getRepositoryId() );
        }
        else
        {
            getNexus().rebuildAttributesAllRepositories( getResourceStorePath() );
        }

        return null;
    }

    protected String getAction()
    {
        return FeedRecorder.SYSTEM_REBUILDATTRIBUTES_ACTION;
    }

    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Rebuilding attributes of repository group with ID=" + getRepositoryGroupId();
        }
        else if ( getRepositoryId() != null )
        {
            return "Rebuilding attributes of repository with ID=" + getRepositoryId();
        }
        else
        {
            return "Rebuilding attributes of all registered repositories";
        }
    }

}
