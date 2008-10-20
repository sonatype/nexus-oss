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

import org.apache.commons.httpclient.HttpException;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
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

/**
 * Creates a few repo targets and make sure the privileges work correctly.
 */
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

    
    
    @Override
    public void resetTestUserPrivs()
        throws Exception
    {
        this.overwriteUserRole( TEST_USER_NAME, "doReadTest-noAccess", "17" );
//      "6", "14","19","44","54","55","56","57","58","64","70"
      this.printUserPrivs( TEST_USER_NAME );
      
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

        // try the groups
        this.groupDownload( repo1BarArtifact, false );
        this.groupDownload( repo2BarArtifact, false );
        this.groupDownload( repo2FooArtifact, true );
        this.groupDownload( repo1FooArtifact, true );
        
        
        this.download( REPO1_ID, repo1BarArtifact, false );
        this.download( REPO2_ID, repo2BarArtifact, false );
        this.download( REPO2_ID, repo2FooArtifact, true ); // this repo is included in a group we have access to
        this.download( REPO1_ID, repo1FooArtifact, true );
        
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
        this.overwriteUserRole( TEST_USER_NAME, "fooPrivUpdateId", this.fooPrivUpdateId, this.fooPrivCreateId );

        TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
        TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

        this.deploy( repo1BarArtifact, REPO1_ID, this.getTestFile( "repo1-bar-artifact.jar" ), false );
        this.deploy( repo1FooArtifact, REPO1_ID, this.getTestFile( "repo1-foo-artifact.jar" ), true );
        this.deploy( repo2BarArtifact, REPO2_ID, this.getTestFile( "repo2-bar-artifact.jar" ), false );
        this.deploy( repo1FooArtifact, REPO2_ID, this.getTestFile( "repo2-foo-artifact.jar" ), false );

        // now give
        this.overwriteUserRole( TEST_USER_NAME, "barPrivUpdateId", this.barPrivUpdateId, this.barPrivCreateId );

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
        this.deploy( repo1FooArtifact, REPO1_ID, this.getTestFile( "repo1-foo-artifact.jar" ), true ); // group contains this repo
        this.deploy( repo2BarArtifact, REPO2_ID, this.getTestFile( "repo2-bar-artifact.jar" ), false );
        this.deploy( repo1FooArtifact, REPO1_ID, this.getTestFile( "repo2-foo-artifact.jar" ), true ); // group contains this repo

    }
     
     @Test
     public void artifactUplaodTest() throws Exception
     {
         TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
         TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

         // test-user should not be able to upload anything.
         this.upload( repo1BarArtifact, REPO1_ID, this.getTestFile( "repo1-bar-artifact.jar" ), false );
         this.upload( repo1FooArtifact, REPO1_ID, this.getTestFile( "repo1-foo-artifact.jar" ), false );
         this.upload( repo2BarArtifact, REPO2_ID, this.getTestFile( "repo2-bar-artifact.jar" ), false );
         this.upload( repo1FooArtifact, REPO2_ID, this.getTestFile( "repo2-foo-artifact.jar" ), false );

         // now give
         this.overwriteUserRole( TEST_USER_NAME, "fooPrivUpdateId", this.fooPrivUpdateId, this.fooPrivCreateId, "65" ); // 65 is upload priv

         TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
         TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

         this.upload( repo1BarArtifact, REPO1_ID, this.getTestFile( "repo1-bar-artifact.jar" ), false );
         this.upload( repo1FooArtifact, REPO1_ID, this.getTestFile( "repo1-foo-artifact.jar" ), true );
         this.upload( repo2BarArtifact, REPO2_ID, this.getTestFile( "repo2-bar-artifact.jar" ), false );
         this.upload( repo1FooArtifact, REPO2_ID, this.getTestFile( "repo2-foo-artifact.jar" ), false );

         // now give
         this.overwriteUserRole( TEST_USER_NAME, "barPrivUpdateId", this.barPrivUpdateId, this.barPrivCreateId, "65" );

         TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
         TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

         this.upload( repo1BarArtifact, REPO1_ID, this.getTestFile( "repo1-bar-artifact.jar" ), true );
         this.upload( repo1FooArtifact, REPO1_ID, this.getTestFile( "repo1-foo-artifact.jar" ), false );
         this.upload( repo2BarArtifact, REPO2_ID, this.getTestFile( "repo2-bar-artifact.jar" ), false );
         this.upload( repo1FooArtifact, REPO2_ID, this.getTestFile( "repo2-foo-artifact.jar" ), false );

         // now give
         this.overwriteUserRole( TEST_USER_NAME, "groupFooPrivUpdateId", this.groupFooPrivUpdateId, "65" );

         TestContainer.getInstance().getTestContext().setUsername( TEST_USER_NAME );
         TestContainer.getInstance().getTestContext().setPassword( TEST_USER_PASSWORD );

         this.upload( repo1BarArtifact, REPO1_ID, this.getTestFile( "repo1-bar-artifact.jar" ), false );
         this.upload( repo1FooArtifact, REPO1_ID, this.getTestFile( "repo1-foo-artifact.jar" ), true );
         this.upload( repo2BarArtifact, REPO2_ID, this.getTestFile( "repo2-bar-artifact.jar" ), false );
         this.upload( repo1FooArtifact, REPO2_ID, this.getTestFile( "repo2-foo-artifact.jar" ), true );
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
        throws InterruptedException, Exception
    {
        try
        {
            
//            DeployUtils.forkDeployWithWagon( this.getContainer(), "http",  this.getRepositoryUrl( repoId ), fileToDeploy, this.getRelitiveArtifactPath( gav ) );
            Verifier verifier = MavenDeployer.deployAndGetVerifier( gav, this.getRepositoryUrl( repoId ), fileToDeploy,
                                  this.getOverridableFile( "settings.xml" ) );
            
            Assert.assertTrue( "Artifact upload should have thrown exception", shouldUpload );
        }
        catch ( VerificationException e )
        {
            Assert.assertFalse( "Artifact should have uploaded: \n" + e.getMessage(), shouldUpload );
        }
            
            // if we made it this far we should also test download, because upload implies download
            this.download( repoId, gav, shouldUpload );

    }
    
    private void upload( Gav gav, String repoId, File fileToDeploy, boolean shouldUpload )
        throws InterruptedException, HttpException, IOException
    {
        int status = DeployUtils.deployUsingGavWithRest( repoId, gav, fileToDeploy );

        Assert.assertTrue( "Artifact upload returned: "+ status + (shouldUpload ? " expected sucess": " expected failure"), (201 == status && shouldUpload) || !shouldUpload );
        // if we made it this far we should also test download, because upload implies download
        this.download( repoId, gav, shouldUpload );
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
            Assert.assertEquals( "Response Status: " + responseText, 204, statusCode );
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

            if ( privilegeBaseStatusResource.getMethod().equals( "create,read" ) )
                fooPrivCreateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "read" ) )
                fooPrivReadId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "update,read" ) )
                fooPrivUpdateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "delete,read" ) )
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

            if ( privilegeBaseStatusResource.getMethod().equals( "create,read" ) )
                barPrivCreateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "read" ) )
                barPrivReadId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "update,read" ) )
                barPrivUpdateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "delete,read" ) )
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
//        groupPriv.setRepositoryId( repositoryId )
//        groupPriv.setName( name )
//        groupPriv.setDescription( description )

        // get the Resource object
        List<PrivilegeBaseStatusResource> groupPrivs = this.privUtil.createPrivileges( groupPriv );

        for ( Iterator<PrivilegeBaseStatusResource> iter = groupPrivs.iterator(); iter.hasNext(); )
        {
            PrivilegeBaseStatusResource privilegeBaseStatusResource = iter.next();

            if ( privilegeBaseStatusResource.getMethod().equals( "create,read" ) )
                groupFooPrivCreateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "read" ) )
                groupFooPrivReadId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "update,read" ) )
                groupFooPrivUpdateId = privilegeBaseStatusResource.getId();
            else if ( privilegeBaseStatusResource.getMethod().equals( "delete,read" ) )
                groupFooPrivDeleteId = privilegeBaseStatusResource.getId();
            else
                Assert.fail( "Unknown Privilege found, id: " + privilegeBaseStatusResource.getId() + " method: "
                    + privilegeBaseStatusResource.getMethod() );
        }

    }

}
