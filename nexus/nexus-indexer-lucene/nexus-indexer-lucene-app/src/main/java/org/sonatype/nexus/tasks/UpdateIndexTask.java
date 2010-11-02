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
package org.sonatype.nexus.tasks;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;
import org.sonatype.nexus.tasks.descriptors.UpdateIndexTaskDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Update index task.
 * 
 * @author cstamas
 * @author velo
 */
@Component( role = SchedulerTask.class, hint = UpdateIndexTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class UpdateIndexTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
{

    @Requirement( role = ReindexTaskHandler.class )
    private List<ReindexTaskHandler> handlers;

    @Override
    protected String getRepositoryFieldId()
    {
        return UpdateIndexTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    @Override
    protected String getRepositoryPathFieldId()
    {
        return UpdateIndexTaskDescriptor.RESOURCE_STORE_PATH_FIELD_ID;
    }

    @Override
    public Object doRun()
        throws Exception
    {
        for ( ReindexTaskHandler handler : handlers )
        {
            if ( getRepositoryId() != null )
            {
                handler.reindexRepository( getRepositoryId(), getResourceStorePath(), false );
            }
            else if ( getRepositoryGroupId() != null )
            {
                handler.reindexRepositoryGroup( getRepositoryGroupId(), getResourceStorePath(), false );
            }
            else
            {
                handler.reindexAllRepositories( getResourceStorePath(), false );
            }
        }

        return null;
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_REINDEX_ACTION;
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Updating repository group index " + getRepositoryGroupName() + " from path "
                + getResourceStorePath()
                + " and below.";
        }
        else if ( getRepositoryId() != null )
        {
            return "Updating repository index " + getRepositoryName() + " from path " + getResourceStorePath()
                + " and below.";
        }
        else
        {
            return "Updating all registered repositories index";
        }
    }

}
