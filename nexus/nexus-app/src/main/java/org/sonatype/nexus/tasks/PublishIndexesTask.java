package org.sonatype.nexus.tasks;

import java.io.IOException;

import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;

/**
 * Publish indexes task.
 * 
 * @author cstamas
 * @plexus.component role="org.sonatype.nexus.tasks.PublishIndexesTask"
 */
public class PublishIndexesTask
    extends AbstractNexusRepositoriesTask<Object>
{
    @Override
    protected Object doRun()
        throws Exception
    {
        try
        {
            if ( getRepositoryId() != null )
            {
                getNexus().publishRepositoryIndex( getRepositoryId() );
            }
            else if ( getRepositoryGroupId() != null )
            {
                getNexus().publishRepositoryGroupIndex( getRepositoryGroupId() );
            }
            else
            {
                getNexus().publishAllIndex();
            }
        }
        catch ( IOException e )
        {
            getLogger().error( "Cannot publish indexes!", e );
        }

        return null;
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
