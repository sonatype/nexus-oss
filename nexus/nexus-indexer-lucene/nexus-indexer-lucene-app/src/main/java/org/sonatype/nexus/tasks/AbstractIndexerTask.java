package org.sonatype.nexus.tasks;

import java.util.List;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;
import org.sonatype.nexus.tasks.descriptors.AbstractIndexTaskDescriptor;

/**
 * @author cstamas
 */
public abstract class AbstractIndexerTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
{

    @Requirement( role = ReindexTaskHandler.class )
    private List<ReindexTaskHandler> handlers;

    private String action;

    private boolean fullReindex;

    public AbstractIndexerTask( String action, boolean fullReindex )
    {
        super();
        this.action = action;
        this.fullReindex = fullReindex;
    }

    @Override
    protected String getRepositoryFieldId()
    {
        return AbstractIndexTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    @Override
    protected String getRepositoryPathFieldId()
    {
        return AbstractIndexTaskDescriptor.RESOURCE_STORE_PATH_FIELD_ID;
    }

    @Override
    public Object doRun()
        throws Exception
    {
        for ( ReindexTaskHandler handler : handlers )
        {
            if ( getRepositoryId() != null )
            {
                handler.reindexRepository( getRepositoryId(), getResourceStorePath(), fullReindex );
            }
            else if ( getRepositoryGroupId() != null )
            {
                handler.reindexRepositoryGroup( getRepositoryGroupId(), getResourceStorePath(), fullReindex );
            }
            else
            {
                handler.reindexAllRepositories( getResourceStorePath(), fullReindex );
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
            return action + " repository group index " + getRepositoryGroupName() + " from path "
                + getResourceStorePath() + " and below.";
        }
        else if ( getRepositoryId() != null )
        {
            return action + " repository index " + getRepositoryName() + " from path " + getResourceStorePath()
                + " and below.";
        }
        else
        {
            return action + " all registered repositories index";
        }
    }

}