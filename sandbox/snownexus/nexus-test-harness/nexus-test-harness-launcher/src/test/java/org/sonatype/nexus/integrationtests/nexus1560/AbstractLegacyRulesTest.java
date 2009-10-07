package org.sonatype.nexus.integrationtests.nexus1560;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public abstract class AbstractLegacyRulesTest
    extends AbstractPrivilegeTest
{

    protected static final String NEXUS1560_GROUP = "nexus1560-group";

    protected Gav gavArtifact1;

    protected Gav gavArtifact2;

    @Before
    public void createGav1()
        throws Exception
    {
        this.gavArtifact1 =
            new Gav( "nexus1560", "artifact", "1.0", null, "jar", null, null, null, false, false, null, false, null );
        this.gavArtifact2 =
            new Gav( "nexus1560", "artifact", "2.0", null, "jar", null, null, null, false, false, null, false, null );
    }

    @Override
    protected void runOnce()
        throws Exception
    {
        super.runOnce();

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "repositoryOrGroupId" );
        repo.setValue( "repo_" + REPO_TEST_HARNESS_REPO );
        TaskScheduleUtil.runTask( "nexus1560-repo", RebuildMavenMetadataTaskDescriptor.ID, repo );
        ScheduledServicePropertyResource repo2 = new ScheduledServicePropertyResource();
        repo2.setId( "repositoryOrGroupId" );
        repo2.setValue( "repo_" + REPO_TEST_HARNESS_REPO2 );
        TaskScheduleUtil.runTask( "nexus1560-repo2", RebuildMavenMetadataTaskDescriptor.ID, repo2 );
    }

    protected Response download( String downloadUrl )
        throws IOException
    {
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        return RequestFacade.doGetRequest( downloadUrl );
    }

    protected Response failDownload( String downloadUrl )
        throws IOException
    {
        Response response = download( downloadUrl );
        Status status = response.getStatus();
        Assert.assertTrue( "Unable to download artifact from repository: " + status, status.isError() );
        return response;
    }

    protected Response successDownload( String downloadUrl )
    throws IOException
    {
        Response response = download( downloadUrl );
        Status status = response.getStatus();
        Assert.assertTrue( "Unable to download artifact from repository: " + status, status.isSuccess() );
        return response;
    }

}