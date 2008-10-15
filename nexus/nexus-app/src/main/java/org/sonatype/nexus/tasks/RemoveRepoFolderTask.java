package org.sonatype.nexus.tasks;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.FeedRecorder;
import org.sonatype.nexus.proxy.repository.RepositoryType;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.nexus.tasks.descriptors.RemoveRepoFolderTaskDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.RepositoryLocalStoragePropertyDescriptor;
import org.sonatype.nexus.tasks.descriptors.properties.RepositoryTypePropertyDescriptor;
import org.sonatype.scheduling.SchedulerTask;

/**
 * Remove repository folder
 * 
 * @author Juven Xu
 */
@Component( role = SchedulerTask.class, hint = RemoveRepoFolderTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class RemoveRepoFolderTask
    extends AbstractNexusRepositoriesTask<Object>
{

    @Override
    protected Object doRun()
        throws Exception
    {
        if ( getRepositoryId() != null )
        {
            getNexus().removeRepositoryFolder( getRepositoryId(), getRepositoryType(), getRepositoryLocalStorage() );
        }
        return null;
    }

    @Override
    protected String getAction()
    {
        return FeedRecorder.SYSTEM_REMOVE_REPO_FOLDER;
    }

    @Override
    protected String getMessage()
    {
        if ( getRepositoryId() != null )
        {
            return "Removing folder with repository ID: " + getRepositoryId();
        }
        return null;
    }

    public void setRepositoryType( RepositoryType type )
    {
        if ( type != null )
        {
            getParameters().put( RepositoryTypePropertyDescriptor.ID, type.name() );
        }
    }

    public RepositoryType getRepositoryType()
    {
        String param = getParameters().get( RepositoryTypePropertyDescriptor.ID );

        if ( param != null )
        {
            return RepositoryType.valueOf( param );
        }

        return null;
    }

    public void setRepositoryLocalStorage( String localStorage )
    {
        if ( localStorage != null )
        {
            getParameters().put( RepositoryLocalStoragePropertyDescriptor.ID, localStorage );
        }
    }

    public String getRepositoryLocalStorage()
    {
        String param = getParameters().get( RepositoryLocalStoragePropertyDescriptor.ID );

        if ( param != null )
        {
            return param;
        }

        return null;
    }
}
