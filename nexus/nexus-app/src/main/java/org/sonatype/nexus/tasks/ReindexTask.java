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
import org.sonatype.nexus.tasks.descriptors.ReindexTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.ForceFullReindexPropertyDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Reindex task.
 *
 * @author cstamas
 */
@Component( role = SchedulerTask.class, hint = ReindexTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class ReindexTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
{
    @Requirement
    private IndexerManager indexerManager;

    public boolean getFullReindex()
    {
        boolean fullReindex = new Boolean( getParameter( ForceFullReindexPropertyDescriptor.ID ) );
        return fullReindex;
    }

    public void setFullReindex( boolean fullReindex )
    {
        getParameters().put( ForceFullReindexPropertyDescriptor.ID, String.valueOf( fullReindex ) );
    }

    @Override
    public Object doRun()
        throws Exception
    {
        if ( getRepositoryId() != null )
        {
            indexerManager.reindexRepository( getResourceStorePath(), getRepositoryId(), getFullReindex()  );
        }
        else if ( getRepositoryGroupId() != null )
        {
            indexerManager.reindexRepositoryGroup( getResourceStorePath(), getRepositoryId(), getFullReindex()  );
        }
        else
        {
            indexerManager.reindexAllRepositories( getResourceStorePath(), getFullReindex() );
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
            return "Reindexing repository group " + getRepositoryGroupName() + " from path " + getResourceStorePath()
                + " and below.";
        }
        else if ( getRepositoryId() != null )
        {
            return "Reindexing repository " + getRepositoryName() + " from path " + getResourceStorePath()
                + " and below.";
        }
        else
        {
            return "Reindexing all registered repositories";
        }
    }

}
