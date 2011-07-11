package org.sonatype.nexus.plugins.p2.repository.internal.tasks;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregator;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;

@Named( P2RepositoryAggregatorTaskDescriptor.ID )
public class P2RepositoryAggregatorTask
    extends AbstractNexusRepositoriesTask<Object>
    implements SchedulerTask<Object>
{

    private final P2RepositoryAggregator p2RepositoryAggregator;

    @Inject
    P2RepositoryAggregatorTask( final P2RepositoryAggregator p2RepositoryAggregator )
    {
        this.p2RepositoryAggregator = p2RepositoryAggregator;
    }

    @Override
    protected String getRepositoryFieldId()
    {
        return P2RepositoryAggregatorTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    @Override
    protected String getAction()
    {
        return "REBUILD";
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryId() != null )
        {
            return String.format( "Rebuild p2 repository on repository [%s] from root path and bellow",
                getRepositoryId() );
        }
        else
        {
            return "Rebuild p2 repository for all repositories (with a P2 Repository Generator Capability enabled)";
        }
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        final String repositoryId = getRepositoryId();
        if ( repositoryId != null )
        {
            p2RepositoryAggregator.scanAndRebuild( repositoryId );
        }
        else
        {
            p2RepositoryAggregator.scanAndRebuild();
        }

        return null;
    }

}
