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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.index.IndexerManager;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.proxy.maven.MavenRepository;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
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

    @Requirement
    private RepositoryRegistry repositoryRegistry;

    @Override
    public Object doRun()
        throws Exception
    {
        List<String> repoIds;
        if ( getRepositoryId() != null )
        {
            repoIds = Collections.singletonList( getRepositoryId() );
        }
        else if ( getRepositoryGroupId() != null )
        {
            repoIds = Collections.singletonList( getRepositoryGroupId() );
        }
        else
        {
            repoIds = new ArrayList<String>();
            for ( Repository repo : repositoryRegistry.getRepositories() )
            {
                if ( repo.getRepositoryKind().isFacetAvailable( MavenRepository.class ) )
                {
                    repoIds.add( repo.getId() );
                }
            }
        }

        for ( String repoId : repoIds )
        {
            // local
            IndexingContext context = indexManager.getRepositoryLocalIndexContext( repoId );
            if ( context != null )
            {
                context.optimize();
            }

            // remote
            context = indexManager.getRepositoryRemoteIndexContext( repoId );
            if ( context != null )
            {
                context.optimize();
            }
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
            return "Optimizing repository group index " + getRepositoryGroupName() + " from path "
                + getResourceStorePath() + " and below.";
        }
        else if ( getRepositoryId() != null )
        {
            return "Optimizing repository index " + getRepositoryName() + " from path " + getResourceStorePath()
                + " and below.";
        }
        else
        {
            return "Optimizing all registered repositories index";
        }
    }

}
