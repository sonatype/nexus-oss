package org.sonatype.nexus.integrationtests.nexus537;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.rest.model.PrivilegeBaseStatusResource;
import org.sonatype.nexus.rest.model.PrivilegeTargetResource;
import org.sonatype.nexus.rest.model.RepositoryTargetResource;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.MavenDeployer;

public class Nexus537RepoTargetsTests
    extends AbstractPrivilegeTest
{
    private String fooPrivCreateId;

    private String fooPrivReadId;

    private String fooPrivUpdateId;

    private String fooPrivDeleteId;

    private String barPrivCreateId;

    private String barPrivReadId;

    private String barPrivUpdateId;

    private String barPrivDeleteId;

    private String groupFooPrivCreateId;

    private String groupFooPrivReadId;

    private String groupFooPrivUpdateId;

    private String groupFooPrivDeleteId;

    private Gav repo1BarArtifact;

    private Gav repo1FooArtifact;

    private Gav repo2BarArtifact;

    private Gav repo2FooArtifact;

    private Gav repo1BarArtifactDelete;

    private Gav repo1FooArtifactDelete;

    private Gav repo2BarArtifactDelete;

    private Gav repo2FooArtifactDelete;

    private static final String REPO1_ID = "repo1";

    private static final String REPO2_ID = "repo2";

    public Nexus537RepoTargetsTests()
    {
        repo1BarArtifact =
            new Gav( this.getTestId(), "repo1-bar-artifact", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "repo1-bar-artifact", false, false, null, false, null );
        repo1FooArtifact =
            new Gav( this.getTestId(), "repo1-foo-artifact", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "repo1-foo-artifact", false, false, null, false, null );
        repo2BarArtifact =
            new Gav( this.getTestId(), "repo2-bar-artifact", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "repo1-bar-artifact", false, false, null, false, null );
        repo2FooArtifact =
            new Gav( this.getTestId(), "repo2-foo-artifact", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "repo1-bar-artifact", false, false, null, false, null );

        repo1BarArtifactDelete =
            new Gav( this.getTestId(), "repo1-bar-artifact-delete", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "repo1-bar-artifact-delete", false, false, null, false, null );
        repo1FooArtifactDelete =
            new Gav( this.getTestId(), "repo1-foo-artifact-delete", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "repo1-foo-artifact-delete", false, false, null, false, null );
        repo2BarArtifactDelete =
            new Gav( this.getTestId(), "repo2-bar-artifact-delete", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "repo1-bar-artifact-delete", false, false, null, false, null );
        repo2FooArtifactDelete =
            new Gav( this.getTestId(), "repo2-foo-artifact-delete", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "repo1-bar-artifact-delete", false, false, null, false, null );
    }

    @Test
    public void doReadTest()
        throws Exception
    {

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // test-user should not be able to download anything.
        this.download( REPO1_ID, repo1BarArtifact, false );
        this.download( REPO1_ID, repo1FooArtifact, false );
        this.download( REPO2_ID, repo2BarArtifact, false );
        this.download( REPO2_ID, repo2FooArtifact, false );

        // now give
        this.overwriteUserRole( TEST_USER_NAME, "fooPrivReadId", this.fooPrivReadId);

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        this.download( REPO1_ID, repo1BarArtifact, false );
        this.download( REPO2_ID, repo2BarArtifact, false );
        this.download( REPO2_ID, repo2FooArtifact, false );
        this.download( REPO1_ID, repo1FooArtifact, true );

        // now give
        this.overwriteUserRole( TEST_USER_NAME, "barPrivReadId", this.barPrivReadId );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        this.download( REPO1_ID, repo1BarArtifact, true );
        this.download( REPO2_ID, repo2BarArtifact, false );
        this.download( REPO2_ID, repo2FooArtifact, false );
        this.download( REPO1_ID, repo1FooArtifact, false );

        // now give
        this.overwriteUserRole( TEST_USER_NAME, "groupPrivReadId", this.groupFooPrivReadId );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // we should not be able to hit any of the repos directly
        this.groupDownload( repo1BarArtifact, false );
        this.groupDownload( repo2BarArtifact, false );
        this.groupDownload( repo2FooArtifact, true );
        this.groupDownload( repo1FooArtifact, true );
        
        this.download( REPO1_ID, repo1BarArtifact, false );
        this.download( REPO2_ID, repo2BarArtifact, false );
        this.download( REPO2_ID, repo2FooArtifact, false );
        this.download( REPO1_ID, repo1FooArtifact, false );
        
    }

     @Test
    public void doCreateTest()
        throws Exception
    {

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        // test-user should not be able to upload anything.
        this.deploy( repo1BarArtifact, REPO1_ID, this.getTestFile( "repo1-bar-artifact.jar" ), false );
        this.deploy( repo1FooArtifact, REPO1_ID, this.getTestFile( "repo1-foo-artifact.jar" ), false );
        this.deploy( repo2BarArtifact, REPO2_ID, this.getTestFile( "repo2-bar-artifact.jar" ), false );
        this.deploy( repo1FooArtifact, REPO2_ID, this.getTestFile( "repo2-foo-artifact.jar" ), false );

        // now give
        this.overwriteUserRole( TEST_USER_NAME, "fooPrivUpdateId", this.fooPrivUpdateId );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        this.deploy( repo1BarArtifact, REPO1_ID, this.getTestFile( "repo1-bar-artifact.jar" ), false );
        this.deploy( repo1FooArtifact, REPO1_ID, this.getTestFile( "repo1-foo-artifact.jar" ), true );
        this.deploy( repo2BarArtifact, REPO2_ID, this.getTestFile( "repo2-bar-artifact.jar" ), false );
        this.deploy( repo1FooArtifact, REPO2_ID, this.getTestFile( "repo2-foo-artifact.jar" ), false );

        // now give
        this.overwriteUserRole( TEST_USER_NAME, "barPrivUpdateId", this.barPrivUpdateId );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        this.deploy( repo1BarArtifact, REPO1_ID, this.getTestFile( "repo1-bar-artifact.jar" ), true );
        this.deploy( repo1FooArtifact, REPO1_ID, this.getTestFile( "repo1-foo-artifact.jar" ), false );
        this.deploy( repo2BarArtifact, REPO2_ID, this.getTestFile( "repo2-bar-artifact.jar" ), false );
        this.deploy( repo1FooArtifact, REPO2_ID, this.getTestFile( "repo2-foo-artifact.jar" ), false );

        // now give
        this.overwriteUserRole( TEST_USER_NAME, "groupFooPrivUpdateId", this.groupFooPrivUpdateId );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        this.deploy( repo1BarArtifact, REPO1_ID, this.getTestFile( "repo1-bar-artifact.jar" ), false );
        this.deploy( repo1FooArtifact, REPO1_ID, this.getTestFile( "repo1-foo-artifact.jar" ), false );
        this.deploy( repo2BarArtifact, REPO2_ID, this.getTestFile( "repo2-bar-artifact.jar" ), false );
        this.deploy( repo1FooArtifact, REPO2_ID, this.getTestFile( "repo2-foo-artifact.jar" ), false );

    }

    @Test
    public void doDeleteTest()
        throws Exception
    {

        // deploy the artifacts first, we need to use different once because i have no idea how to order the tests with
        // JUnit
        DeployUtils.deployUsingGavWithRest( REPO1_ID, repo1BarArtifactDelete,
                                            this.getTestFile( "repo1-bar-artifact.jar" ) );
        DeployUtils.deployUsingGavWithRest( REPO1_ID, repo1FooArtifactDelete,
                                            this.getTestFile( "repo1-foo-artifact.jar" ) );
        DeployUtils.deployUsingGavWithRest( REPO2_ID, repo2BarArtifactDelete,
                                            this.getTestFile( "repo2-bar-artifact.jar" ) );
        DeployUtils.deployUsingGavWithRest( REPO2_ID, repo2FooArtifactDelete,
                                            this.getTestFile( "repo2-foo-artifact.jar" ) );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        this.delete( repo1BarArtifactDelete, REPO1_ID, false );
        this.delete( repo1FooArtifactDelete, REPO1_ID, false );
        this.delete( repo2BarArtifactDelete, REPO2_ID, false );
        this.delete( repo2FooArtifactDelete, REPO2_ID, false );

        // now give
        this.overwriteUserRole( TEST_USER_NAME, "fooPrivDeleteId", this.fooPrivDeleteId );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        this.delete( repo1BarArtifactDelete, REPO1_ID, false );
        this.delete( repo1FooArtifactDelete, REPO1_ID, true );
        this.delete( repo2BarArtifactDelete, REPO2_ID, false );
        this.delete( repo2FooArtifactDelete, REPO2_ID, false );

        // now give
        this.overwriteUserRole( TEST_USER_NAME, "fooPrivDeleteId", this.barPrivDeleteId );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        this.delete( repo1BarArtifactDelete, REPO1_ID, true );
        this.delete( repo1FooArtifactDelete, REPO1_ID, false );
        this.delete( repo2BarArtifactDelete, REPO2_ID, false );
        this.delete( repo2FooArtifactDelete, REPO2_ID, false );

    }


    private File download( String repoId, Gav gav, boolean shouldDownload )
    {
        File result = null;
        try
        {
            result = this.downloadArtifactFromRepository( repoId, gav, "target/nexus537jars/" );
            Assert.assertTrue( "Artifact download should have thrown exception", shouldDownload );
        }
        catch ( IOException e )
        {
            Assert.assertFalse( "Artifact should have downloaded: \n" + e.getMessage(), shouldDownload );
        }

        return result;
    }

    private void deploy( Gav gav, String repoId, File fileToDeploy, boolean shouldUpload )
        throws InterruptedException
    {
        try
        {
            MavenDeployer.deploy( gav, this.getRepositoryUrl( repoId ), fileToDeploy,
                                  this.getOverridableFile( "settings.xml" ) );
            Assert.assertTrue( "Artifact upload should have thrown exception", shouldUpload );
            // if we made it this far we should also test download, because upload implies download
            this.download( repoId, gav, shouldUpload );
        }
        catch ( CommandLineException e )
        {
            Assert.assertFalse( "Artifact should have uploaded: \n" + e.getMessage(), shouldUpload );
        }
    }

    private void delete( Gav gav, String repoId, boolean shouldDelete )
        throws IOException
    {
        URL url = new URL( this.getRepositoryUrl( repoId ) + this.getRelitiveArtifactPath( gav ) );

        int initialGet = RequestFacade.sendMessage( url, Method.GET, null ).getStatus().getCode();

        Response reponse = RequestFacade.sendMessage( url, Method.DELETE, null );
        String responseText = reponse.getEntity().getText();
        int statusCode = reponse.getStatus().getCode();

        if ( !shouldDelete )
        {
            Assert.assertEquals( "Response Status: " + responseText, 401, statusCode );
        }
        else
        {
            Assert.assertEquals( "Response Status: " + responseText, 200, statusCode );
            Assert.assertEquals( "GET of artifact before DELETE:", 200, initialGet );
            // we should have read also
            reponse = RequestFacade.sendMessage( url, Method.GET, null );
            responseText = reponse.getEntity().getText();
            statusCode = reponse.getStatus().getCode();
            Assert.assertEquals( "File should have been deleted from: " + url + "\n" + responseText, 404, statusCode );
        }

    }
    
    private File groupDownload( Gav gav, boolean shouldDownload )
    {
        File result = null;
        try
        {
            result = this.downloadArtifactFromGroup( "test-group", gav, "target/nexus537jars/" );
            Assert.assertTrue( "Artifact download should have thrown exception", shouldDownload );
        }
        catch ( IOException e )
        {
            Assert.assertFalse( "Artifact should have downloaded: \n" + e.getMessage(), shouldDownload );
        }

        return result;
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest#oncePerClassSetUp()
     */
    public void oncePerClassSetUp()
        throws Exception
    {
        super.oncePerClassSetUp();

        // create my repo targets
        RepositoryTargetResource fooTarget = new RepositoryTargetResource();
        fooTarget.setContentClass( "maven2" );
        fooTarget.setName( "Foo" );
        fooTarget.addPattern( ".*nexu.537/repo.-foo.*" );
        fooTarget = this.targetUtil.createTarget( fooTarget );

        RepositoryTargetResource barTarget = new RepositoryTargetResource();
        barTarget.setContentClass( "maven2" );
        barTarget.setName( "Bar" );
        barTarget.addPattern( ".*nexu.537/repo.-bar.*" );
        barTarget = this.targetUtil.createTarget( barTarget );

        // now create a couple privs
        PrivilegeTargetResource fooPriv = new PrivilegeTargetResource();
        fooPriv.addMethod( "create" );
        fooPriv.addMethod( "read" );
        fooPriv.addMethod( "update" );
        fooPriv.addMethod( "delete" );
        fooPriv.setName( "FooPriv" );
        fooPriv.setType( "repositoryTarget" );
        fooPriv.setRepositoryTargetId( fooTarget.getId() );
        fooPriv.setRepositoryId( "repo1" );
        // get the Resource object
        List<PrivilegeBaseStatusResource> fooPrivs = this.privUtil.createPrivileges( fooPriv );

        for ( Iterator<PrivilegeBaseStatusResource> iter = fooPrivs.iterator(); iter.hasNext(); )
        {
            PrivilegeBaseStatusResource privilegeBaseStatusResource = iter.next();

            if ( privilegeBaseStatusResource.getMethod().equals( "create" ) )
                fooPrivCreateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "read" ) )
                fooPrivReadId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "update" ) )
                fooPrivUpdateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "delete" ) )
                fooPrivDeleteId = privilegeBaseStatusResource.getId();
            else
                Assert.fail( "Unknown Privilege found, id: " + privilegeBaseStatusResource.getId() + " method: "
                    + privilegeBaseStatusResource.getMethod() );
        }

        // now create a couple privs
        PrivilegeTargetResource barPriv = new PrivilegeTargetResource();
        barPriv.addMethod( "create" );
        barPriv.addMethod( "read" );
        barPriv.addMethod( "update" );
        barPriv.addMethod( "delete" );
        barPriv.setName( "BarPriv" );
        barPriv.setType( "repositoryTarget" );
        barPriv.setRepositoryTargetId( barTarget.getId() );
        barPriv.setRepositoryId( "repo1" );

        // get the Resource object
        List<PrivilegeBaseStatusResource> barPrivs = this.privUtil.createPrivileges( barPriv );

        for ( Iterator<PrivilegeBaseStatusResource> iter = barPrivs.iterator(); iter.hasNext(); )
        {
            PrivilegeBaseStatusResource privilegeBaseStatusResource = iter.next();

            if ( privilegeBaseStatusResource.getMethod().equals( "create" ) )
                barPrivCreateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "read" ) )
                barPrivReadId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "update" ) )
                barPrivUpdateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "delete" ) )
                barPrivDeleteId = privilegeBaseStatusResource.getId();
            else
                Assert.fail( "Unknown Privilege found, id: " + privilegeBaseStatusResource.getId() + " method: "
                    + privilegeBaseStatusResource.getMethod() );
        }

        // now create a couple privs
        PrivilegeTargetResource groupPriv = new PrivilegeTargetResource();
        groupPriv.addMethod( "create" );
        groupPriv.addMethod( "read" );
        groupPriv.addMethod( "update" );
        groupPriv.addMethod( "delete" );
        groupPriv.setName( "GroupPriv" );
        groupPriv.setType( "repositoryTarget" );
        groupPriv.setRepositoryTargetId( fooTarget.getId() );
        groupPriv.setRepositoryGroupId( "test-group" );

        // get the Resource object
        List<PrivilegeBaseStatusResource> groupPrivs = this.privUtil.createPrivileges( groupPriv );

        for ( Iterator<PrivilegeBaseStatusResource> iter = groupPrivs.iterator(); iter.hasNext(); )
        {
            PrivilegeBaseStatusResource privilegeBaseStatusResource = iter.next();

            if ( privilegeBaseStatusResource.getMethod().equals( "create" ) )
                groupFooPrivCreateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "read" ) )
                groupFooPrivReadId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "update" ) )
                groupFooPrivUpdateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "delete" ) )
                groupFooPrivDeleteId = privilegeBaseStatusResource.getId();
            else
                Assert.fail( "Unknown Privilege found, id: " + privilegeBaseStatusResource.getId() + " method: "
                    + privilegeBaseStatusResource.getMethod() );
        }

    }

}
