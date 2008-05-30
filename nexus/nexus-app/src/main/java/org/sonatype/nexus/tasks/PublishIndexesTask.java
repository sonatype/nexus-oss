package org.sonatype.nexus.tasks;

import java.io.IOException;

import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.index.AbstractIndexerTask;
import org.sonatype.nexus.index.IndexerManager;

public class PublishIndexesTask
    extends AbstractIndexerTask
{
    public PublishIndexesTask( Nexus nexus, IndexerManager indexerManager, String repositoryId, String repositoryGroupId )
    {
        super( nexus, indexerManager, repositoryId, repositoryGroupId );
    }

    @Override
    protected void doRun()
        throws Exception
    {
        try
        {
            if ( getRepositoryId() != null )
            {
                getIndexerManager().publishRepositoryIndex( getRepositoryId() );
            }
            else if ( getRepositoryGroupId() != null )
            {
                getIndexerManager().publishRepositoryGroupIndex( getRepositoryGroupId() );
            }
            else
            {
                getIndexerManager().publishAllIndex();
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot publish indexes!", e );
        }
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_PUBLISHINDEX_ACTION;
    }

    @Override
    protected String getMessage()
    {
        return "Publishing indexes for all registered repositories.";
    }

}
