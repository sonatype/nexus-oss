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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.Repository;
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
        ResourceStoreRequest req = new ResourceStoreRequest( getResourceStorePath() );

        Map<String, String> initialData = new HashMap<String, String>();

        if ( getRepositoryGroupId() != null )
        {
            getRepositoryRegistry()
                .getRepositoryWithFacet( getRepositoryGroupId(), GroupRepository.class ).recreateAttributes(
                    req,
                    initialData );
        }
        else if ( getRepositoryId() != null )
        {
            getRepositoryRegistry().getRepositoryWithFacet( getRepositoryId(), Repository.class ).recreateAttributes(
                req,
                initialData );
        }
        else
        {
            getNexus().rebuildAttributesAllRepositories( req );
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
            return "Rebuilding attributes of repository group " + getRepositoryGroupName();
        }
        else if ( getRepositoryId() != null )
        {
            return "Rebuilding attributes of repository " + getRepositoryName();
        }
        else
        {
            return "Rebuilding attributes of all registered repositories";
        }
    }

}
