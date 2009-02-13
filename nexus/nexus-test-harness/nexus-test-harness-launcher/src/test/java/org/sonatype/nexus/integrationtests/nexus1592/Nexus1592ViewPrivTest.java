package org.sonatype.nexus.integrationtests.nexus1592;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.jsecurity.realms.TargetPrivilegeDescriptor;
import org.sonatype.nexus.test.utils.DeployUtils;

public class Nexus1592ViewPrivTest
    extends AbstractPrivilegeTest
{

    private Gav downloadGav;

    private String downloadUrl;

    private File fileToDeploy;

    private Gav deployGav;

    private String deleteUri;

    private Gav deleteGav;

    @Before
    public void createGav()
        throws Exception
    {
        this.downloadGav =
            new Gav( "nexus1592", "artifact", "1.0", null, "jar", null, null, null, false, false, null, false, null );
        this.deployGav =
            new Gav( "nexus1592", "artifact", "2.0", null, "jar", null, null, null, false, false, null, false, null );
        this.deleteGav =
            new Gav( "nexus1592", "delete-artifact", "1.0", null, "jar", null, null, null, false, false, null, false,
                     null );

        downloadUrl = REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO + "/" + getRelitiveArtifactPath( downloadGav );
        deleteUri = REPOSITORY_RELATIVE_URL + REPO_TEST_HARNESS_REPO + "/" + getRelitiveArtifactPath( deleteGav );

        fileToDeploy = getTestFile( "artifact.jar" );
    }

    protected int deploy( Gav gav )
        throws IOException
    {
        this.giveUserPrivilege( TEST_USER_NAME, "65" );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        return DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy );
    }

    protected int failDeploy( Gav gav )
        throws IOException
    {
        int response = deploy( gav );
        Assert.assertTrue( "Unable to deploy artifact: " + response, Status.isError( response ) );
        return response;
    }

    protected int successDeploy( Gav gav )
        throws IOException
    {
        int response = deploy( gav );
        Assert.assertTrue( "Unable to deploy artifact: " + response, Status.isSuccess( response ) );
        return response;
    }

    protected Response download()
        throws IOException
    {
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        return RequestFacade.doGetRequest( downloadUrl );
    }

    protected Response failDownload()
        throws IOException
    {
        Response response = download();
        Status status = response.getStatus();
        Assert.assertTrue( "Unable to download artifact from repository: " + status, status.isError() );
        return response;
    }

    protected Response successDownload()
        throws IOException
    {
        Response response = download();
        Status status = response.getStatus();
        Assert.assertTrue( "Unable to download artifact from repository: " + status, status.isSuccess() );
        return response;
    }

    protected Response delete()
        throws IOException
    {
        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        return RequestFacade.sendMessage( deleteUri, Method.DELETE );
    }

    protected Response failDelete()
        throws IOException
    {
        Response response = delete();
        Status status = response.getStatus();
        Assert.assertTrue( "Unable to download artifact from repository: " + status, status.isError() );
        return response;
    }

    protected Response successDelete()
        throws IOException
    {
        Response response = delete();
        Status status = response.getStatus();
        Assert.assertTrue( "Unable to download artifact from repository: " + status, status.isSuccess() );
        return response;
    }

    @Test
    public void deployNoPrivs()
        throws Exception
    {
        failDeploy( deployGav );
    }

    @Test
    public void deployCreate()
        throws Exception
    {
        addPriv( TEST_USER_NAME, REPO_TEST_HARNESS_REPO + "-priv", TargetPrivilegeDescriptor.TYPE, "1",
                 REPO_TEST_HARNESS_REPO, null, "create" );

        failDeploy( deployGav );
    }

    @Test
    public void deployView()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );

        failDeploy( deployGav );
    }

    @Test
    public void deployViewUpdate()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );
        addPriv( TEST_USER_NAME, REPO_TEST_HARNESS_REPO + "-priv", TargetPrivilegeDescriptor.TYPE, "1",
                 REPO_TEST_HARNESS_REPO, null, "update" );

        failDeploy( deployGav );
    }

    @Test
    public void deployViewCreate()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );
        addPriv( TEST_USER_NAME, REPO_TEST_HARNESS_REPO + "-priv", TargetPrivilegeDescriptor.TYPE, "1",
                 REPO_TEST_HARNESS_REPO, null, "create" );

        successDeploy( deployGav );
    }

    @Test
    public void downloadNoPrivs()
        throws Exception
    {
        failDownload();
    }

    @Test
    public void downloadRead()
        throws Exception
    {
        addPriv( TEST_USER_NAME, REPO_TEST_HARNESS_REPO + "-priv", TargetPrivilegeDescriptor.TYPE, "1",
                 REPO_TEST_HARNESS_REPO, null, "read" );

        failDownload();
    }

    @Test
    public void downloadView()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );

        failDownload();
    }

    @Test
    public void downloadViewRead()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );
        addPriv( TEST_USER_NAME, REPO_TEST_HARNESS_REPO + "-priv", TargetPrivilegeDescriptor.TYPE, "1",
                 REPO_TEST_HARNESS_REPO, null, "read" );

        successDownload();
    }

    @Test
    public void redeployNoPrivs()
        throws Exception
    {
        failDeploy( downloadGav );
    }

    @Test
    public void redeployUpdate()
        throws Exception
    {
        addPriv( TEST_USER_NAME, REPO_TEST_HARNESS_REPO + "-priv", TargetPrivilegeDescriptor.TYPE, "1",
                 REPO_TEST_HARNESS_REPO, null, "update" );

        failDeploy( downloadGav );
    }

    @Test
    public void redeployView()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );

        failDeploy( downloadGav );
    }

    @Test
    public void redeployViewCreate()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );
        addPriv( TEST_USER_NAME, REPO_TEST_HARNESS_REPO + "-priv", TargetPrivilegeDescriptor.TYPE, "1",
                 REPO_TEST_HARNESS_REPO, null, "read" );

        failDeploy( downloadGav );
    }

    @Test
    public void redeployViewUpdate()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );
        addPriv( TEST_USER_NAME, REPO_TEST_HARNESS_REPO + "-priv", TargetPrivilegeDescriptor.TYPE, "1",
                 REPO_TEST_HARNESS_REPO, null, "update" );

        successDeploy( downloadGav );
    }

    @Test
    public void deleteNoPrivs()
        throws Exception
    {
        failDelete();
    }

    @Test
    public void deleteDelete()
        throws Exception
    {
        addPriv( TEST_USER_NAME, REPO_TEST_HARNESS_REPO + "-priv", TargetPrivilegeDescriptor.TYPE, "1",
                 REPO_TEST_HARNESS_REPO, null, "delete" );

        failDelete();
    }

    @Test
    public void deleteView()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );

        failDelete();
    }

    @Test
    public void deleteViewDelete()
        throws Exception
    {
        this.giveUserPrivilege( TEST_USER_NAME, "repository-" + REPO_TEST_HARNESS_REPO );
        addPriv( TEST_USER_NAME, REPO_TEST_HARNESS_REPO + "-priv", TargetPrivilegeDescriptor.TYPE, "1",
                 REPO_TEST_HARNESS_REPO, null, "delete" );

        successDelete();
    }

}
