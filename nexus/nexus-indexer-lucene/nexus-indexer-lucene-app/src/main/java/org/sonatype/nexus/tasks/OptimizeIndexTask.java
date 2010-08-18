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

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;
import org.sonatype.nexus.tasks.descriptors.OptimizeIndexTaskDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * OptimizeIndex task.
 * 
 * @author cstamas
 */
@Component( role = SchedulerTask.class, hint = OptimizeIndexTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class OptimizeIndexTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
{

    @Requirement
    private IndexerManager indexManager;

    @Override
    protected String getRepositoryFieldId()
    {
        return OptimizeIndexTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }
    
    @Override
    protected String getRepositoryPathFieldId()
    {
        return null;
    }

    @Override
    public Object doRun()
        throws Exception
    {
        if ( getRepositoryId() != null )
        {
            indexManager.optimizeRepositoryIndex( getRepositoryId() );
        }
        else if ( getRepositoryGroupId() != null )
        {
            indexManager.optimizeGroupIndex( getRepositoryGroupId() );
        }
        else
        {
            indexManager.optimizeAllRepositoriesIndex();
        }

        return null;
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_OPTIMIZE_INDEX_ACTION;
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryGroupId() != null )
        {
            return "Optimizing repository group " + getRepositoryGroupName() + " index from path "
                + getResourceStorePath() + " and below.";
        }
        else if ( getRepositoryId() != null )
        {
            return "Optimizing repository " + getRepositoryName() + " index from path " + getResourceStorePath()
                + " and below.";
        }
        else
        {
            return "Optimizing all maven repositories indexes";
        }
    }

}
