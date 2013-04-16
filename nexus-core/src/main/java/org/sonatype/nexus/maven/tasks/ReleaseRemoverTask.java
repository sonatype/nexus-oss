package org.sonatype.nexus.maven.tasks;

import static com.google.common.base.Preconditions.checkNotNull;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.maven.tasks.descriptors.ReleaseRemovalTaskDescriptor;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesTask;
import org.sonatype.scheduling.SchedulerTask;

/**
 * @since 2.5
 */
//@Named(ReleaseRemovalTaskDescriptor.ID)
//public class ReleaseRemoverTask
//    extends AbstractNexusRepositoriesTask<ReleaseRemovalResult>
//{
//
//    private final Provider<ReleaseRemover> releaseRemoverProvider;
//
//    @Inject
//    public ReleaseRemoverTask( final Provider<ReleaseRemover> releaseRemoverProvider )
//    {
//        this.releaseRemoverProvider = checkNotNull( releaseRemoverProvider );
//    }
@Component( role = SchedulerTask.class, hint = ReleaseRemovalTaskDescriptor.ID, instantiationStrategy = "per-lookup" )
public class ReleaseRemoverTask extends AbstractNexusRepositoriesTask<ReleaseRemovalResult>
{
    private static final String ACTION = "REMOVERELEASES";

    private final ReleaseRemover releaseRemover;

    @Inject
    public ReleaseRemoverTask( final ReleaseRemover releaseRemover )
    {
        this.releaseRemover = checkNotNull(releaseRemover);
    }

    @Override
    protected String getRepositoryFieldId()
    {
        return ReleaseRemovalTaskDescriptor.REPOSITORY_FIELD_ID;
    }

    @Override
    protected ReleaseRemovalResult doRun()
        throws Exception
    {
        int numberOfVersionsToKeep = Integer.parseInt(
            getParameter( ReleaseRemovalTaskDescriptor.NUMBER_OF_VERSIONS_TO_KEEP_FIELD_ID ) );
        return releaseRemover.removeReleases(
            new ReleaseRemovalRequest( getRepositoryId(), numberOfVersionsToKeep ) );
    }

    @Override
    protected String getAction()
    {
        return getClass().getSimpleName();
    }

    @Override
    protected String getMessage()
    {
        return "Removing old releases from repository " + getRepositoryName();
    }
}
