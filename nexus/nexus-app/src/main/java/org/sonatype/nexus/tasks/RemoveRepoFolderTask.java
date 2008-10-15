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
        return FeedRecorder.SYSTEM_REMOVE_REPO_FOLDER;
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
