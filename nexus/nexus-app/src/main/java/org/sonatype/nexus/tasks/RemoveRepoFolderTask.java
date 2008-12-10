/**
 * Sonatype NexusTM [Open Source Version].
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.scheduling.AbstractNexusTask;
import org.sonatype.nexus.tasks.descriptors.RemoveRepoFolderTaskDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Remove repository folder
 * 
 * @author Juven Xu
 */
@Component( role = SchedulerTask.class, hint = RemoveRepoFolderTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class RemoveRepoFolderTask
    extends AbstractNexusTask<Object>
{
    private Repository repository;

    public Repository getRepository()
    {
        return repository;
    }

    public void setRepository( Repository repository )
    {
        this.repository = repository;
    }
    
    @Override
    public boolean isExposed()
    {
        return false;
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        if ( repository != null )
        {
            getNexus().removeRepositoryFolder( repository );
        }
        return null;
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_REMOVE_REPO_FOLDER_ACTION;
    }

    @Override
    protected String getMessage()
    {
        if ( repository != null )
        {
            return "Removing folder with repository ID: " + repository.getId();
        }
        return null;
    }

}
