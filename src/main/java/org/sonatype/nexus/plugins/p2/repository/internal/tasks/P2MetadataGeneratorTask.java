package org.sonatype.nexus.plugins.p2.repository.internal.tasks;

import javax.inject.Inject;
import javax.inject.Named;

import org.sonatype.nexus.plugins.p2.repository.P2MetadataGenerator;
import org.sonatype.nexus.scheduling.AbstractNexusRepositoriesPathAwareTask;
import org.sonatype.scheduling.SchedulerTask;

@Named( P2MetadataGeneratorTaskDescriptor.ID )
public class P2MetadataGeneratorTask
    extends AbstractNexusRepositoriesPathAwareTask<Object>
    implements SchedulerTask<Object>
{

    private final P2MetadataGenerator p2MetadataGenerator;

    @Inject
    P2MetadataGeneratorTask( final P2MetadataGenerator p2MetadataGenerator )
    {
        this.p2MetadataGenerator = p2MetadataGenerator;
    }

    @Override
    protected String getRepositoryFieldId()
    {
        return P2MetadataGeneratorTaskDescriptor.REPO_OR_GROUP_FIELD_ID;
    }

    @Override
    protected String getRepositoryPathFieldId()
    {
        return P2MetadataGeneratorTaskDescriptor.RESOURCE_STORE_PATH_FIELD_ID;
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
            return String.format( "Rebuild repository [%s] p2 metadata from path [%s] and bellow", getRepositoryId(),
                getResourceStorePath() );
        }
        else
        {
            return "Rebuild p2 metadata from all repositories (with a P2 Metadata Generator Capability enabled)";
        }
    }

    @Override
    protected Object doRun()
        throws Exception
    {
        final String repositoryId = getRepositoryId();
        if ( repositoryId != null )
        {
            p2MetadataGenerator.scanAndRebuild( repositoryId, getResourceStorePath() );
        }
        else
        {
            p2MetadataGenerator.scanAndRebuild( getResourceStorePath() );
        }

        return null;
    }

}
