package org.sonatype.nexus.integrationtests.nexus2351;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import junit.framework.Assert;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.it.VerificationException;
import org.apache.maven.it.Verifier;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractMavenNexusIT;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;
import org.sonatype.nexus.rest.model.RepositoryResource;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;

public class Nexus2351DisableRedeployUploadTest
    extends AbstractMavenNexusIT
{

    private RepositoryMessageUtil repoUtil = null;

    public Nexus2351DisableRedeployUploadTest()
        throws ComponentLookupException
    {
        this.repoUtil = new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML, this
            .getRepositoryTypeRegistry() );
    }

    @Test
    public void disableReleaseAllowRedeployWithMavenTest()
        throws Exception
    {
        RepositoryResource repo = (RepositoryResource) this.repoUtil.getRepository( this.getTestRepositoryId() );
        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        repo = (RepositoryResource) this.repoUtil.updateRepo( repo );

        Gav gav1 = new Gav( this.getTestId(), "release-deploy", "1.0.0", null, "jar", 0, new Date()
            .getTime(), "release-deploy", false, false, null, false, null );
        
        Gav gav2 = new Gav( this.getTestId(), "release-deploy", "1.0.1", null, "jar", 0, new Date()
        .getTime(), "release-deploy", false, false, null, false, null );

        File mavenProject1 = getTestFile( "maven-project-1" );
        File mavenProject2 = getTestFile( "maven-project-2" );
        
        this.deployWithMavenExpectSuccess( mavenProject1 );
        Metadata metadata = this.downloadMetadataFromRepository( gav1, this.getTestRepositoryId() );
        Date firstDeployDate = this.getLastDeployTimeStamp( metadata );
        // we need to sleep 1 second, because we are dealing with a one second accuracy
        Thread.sleep( 1000 );

        this.deployWithMavenExpectSuccess( mavenProject1 );
        metadata = this.downloadMetadataFromRepository( gav1, this.getTestRepositoryId() );
        Date secondDeployDate = this.getLastDeployTimeStamp( metadata );
        Assert.assertTrue( "deploy date was not updated, or is incorrect, first: " + firstDeployDate + " second: "
            + secondDeployDate, firstDeployDate.before( secondDeployDate ) );
        // we need to sleep 1 second, because we are dealing with a one second accuracy
        Thread.sleep( 1000 );

        this.deployWithMavenExpectSuccess( mavenProject1 );
        metadata = this.downloadMetadataFromRepository( gav1, this.getTestRepositoryId() );
        Date thirdDeployDate = this.getLastDeployTimeStamp( metadata );
        Assert.assertTrue( "deploy date was not updated, or is incorrect, second: " + firstDeployDate + " third: "
            + secondDeployDate, secondDeployDate.before( thirdDeployDate ) );
        
        this.deployWithMavenExpectSuccess( mavenProject2 );
        metadata = this.downloadMetadataFromRepository( gav2, this.getTestRepositoryId() );
        
        // now check the metadata for both versions
        Assert.assertTrue(  metadata.getVersioning().getVersions().contains( "1.0.0" ) );
        Assert.assertTrue(  metadata.getVersioning().getVersions().contains( "1.0.1" ) );
        
        Assert.assertEquals( 2, metadata.getVersioning().getVersions().size() );
        
        
    }
    
//    @Test FIXME: BROKEN NEXUS-2395
    public void disableReleaseAllowRedeployWithUploadTest()
        throws Exception
    {
        RepositoryResource repo = (RepositoryResource) this.repoUtil.getRepository( this.getTestRepositoryId() );
        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE.name() );
        repo = (RepositoryResource) this.repoUtil.updateRepo( repo );

        Gav gav = new Gav( this.getTestId(), "release-deploy", "1.0.0", null, "jar", 0, new Date()
            .getTime(), "release-deploy", false, false, null, false, null );

        File fileToDeploy = getTestFile( "artifact.jar" );

        Assert.assertEquals( 201, DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy ) );
        Metadata metadata = this.downloadMetadataFromRepository( gav, this.getTestRepositoryId() );
        Date firstDeployDate = this.getLastDeployTimeStamp( metadata );
        // we need to sleep 1 second, because we are dealing with a one second accuracy
        Thread.sleep( 1000 );

        Assert.assertEquals( 201, DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy ) );
        metadata = this.downloadMetadataFromRepository( gav, this.getTestRepositoryId() );
        Date secondDeployDate = this.getLastDeployTimeStamp( metadata );
        Assert.assertTrue( "deploy date was not updated, or is incorrect, first: " + firstDeployDate + " second: "
            + secondDeployDate, firstDeployDate.before( secondDeployDate ) );
        // we need to sleep 1 second, because we are dealing with a one second accuracy
        Thread.sleep( 1000 );

        Assert.assertEquals( 201, DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy ) );
        metadata = this.downloadMetadataFromRepository( gav, this.getTestRepositoryId() );
        Date thirdDeployDate = this.getLastDeployTimeStamp( metadata );
        Assert.assertTrue( "deploy date was not updated, or is incorrect, second: " + firstDeployDate + " third: "
            + secondDeployDate, secondDeployDate.before( thirdDeployDate ) );
    }

    @Test
    public void disableReleaseReadOnlyWithUploadTest()
        throws Exception
    {
        RepositoryResource repo = (RepositoryResource) this.repoUtil.getRepository( this.getTestRepositoryId() );
        repo.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );
        repo = (RepositoryResource) this.repoUtil.updateRepo( repo );

        Gav gav = new Gav(
            this.getTestId(),
            "disableReleaseReadOnlyWithUploadTest",
            "1.0.0",
            null,
            "jar",
            0,
            new Date().getTime(),
            "disableReleaseReadOnlyWithUploadTest",
            false,
            false,
            null,
            false,
            null );

        File fileToDeploy = getTestFile( "artifact.jar" );

        Assert.assertEquals( 400, DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy ) );
        Assert.assertEquals( 400, DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy ) );
    }
    
    @Test
    public void disableReleaseReadOnlyWithMavenTest()
        throws Exception
    {
        RepositoryResource repo = (RepositoryResource) this.repoUtil.getRepository( this.getTestRepositoryId() );
        repo.setWritePolicy( RepositoryWritePolicy.READ_ONLY.name() );
        repo = (RepositoryResource) this.repoUtil.updateRepo( repo );

        Gav gav = new Gav(
            this.getTestId(),
            "release-deploy",
            "1.0.0",
            null,
            "jar",
            0,
            new Date().getTime(),
            "release-deploy",
            false,
            false,
            null,
            false,
            null );

        File mavenProject = getTestFile( "maven-project-1" );
        
        this.deployWithMavenExpectFailure(  mavenProject );
        this.deployWithMavenExpectFailure(  mavenProject );
    }

//  @Test FIXME: BROKEN NEXUS-2351
    public void disableReleaseNoRedeployWithUploadTest()
        throws Exception
    {
        RepositoryResource repo = (RepositoryResource) this.repoUtil.getRepository( this.getTestRepositoryId() );
        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE_ONCE.name() );
        repo = (RepositoryResource) this.repoUtil.updateRepo( repo );

        Gav gav = new Gav( this.getTestId(), "disableReleaseNoRedeployTest", "1.0.0", null, "jar", 0, new Date()
            .getTime(), "disableReleaseNoRedeployTest", false, false, null, false, null );

        File fileToDeploy = getTestFile( "artifact.jar" );

        Assert.assertEquals( 201, DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy ) );
        Assert.assertEquals( 400, DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy ) );
        Assert.assertEquals( 400, DeployUtils.deployUsingGavWithRest( this.getTestRepositoryId(), gav, fileToDeploy ) );
    }
    
//  @Test FIXME: BROKEN NEXUS-2351
    public void disableReleaseNoRedeployWithMavenTest()
        throws Exception
    {
        RepositoryResource repo = (RepositoryResource) this.repoUtil.getRepository( this.getTestRepositoryId() );
        repo.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE_ONCE.name() );
        repo = (RepositoryResource) this.repoUtil.updateRepo( repo );

        Gav gav1 = new Gav( this.getTestId(), "disableReleaseNoRedeployTest", "1.0.0", null, "jar", 0, new Date()
            .getTime(), "disableReleaseNoRedeployTest", false, false, null, false, null );
        

        Gav gav2 = new Gav( this.getTestId(), "disableReleaseNoRedeployTest", "1.0.1", null, "jar", 0, new Date()
            .getTime(), "disableReleaseNoRedeployTest", false, false, null, false, null );

        File mavenProject1 = getTestFile( "maven-project-1" );
        File mavenProject2 = getTestFile( "maven-project-2" );
        
//        (deploy whould work, once)
        this.deployWithMavenExpectSuccess( mavenProject1 );
        Metadata metadata = this.downloadMetadataFromRepository( gav1, this.getTestRepositoryId() );
        Date firstDeployDate = this.getLastDeployTimeStamp( metadata );
        // we need to sleep 1 second, because we are dealing with a one second accuracy
        Thread.sleep( 1000 );
        
        // deploy again (should fail)
        this.deployWithMavenExpectFailure( mavenProject1 );
        metadata = this.downloadMetadataFromRepository( gav1, this.getTestRepositoryId() );
        Assert.assertEquals( firstDeployDate, this.getLastDeployTimeStamp( metadata ) );
        
        // deploy a new version
        this.deployWithMavenExpectSuccess( mavenProject2 );
        metadata = this.downloadMetadataFromRepository( gav2, this.getTestRepositoryId() );
        
        // now check the metadata for both versions
        Assert.assertTrue(  metadata.getVersioning().getVersions().contains( "1.0.0" ) );
        Assert.assertTrue(  metadata.getVersioning().getVersions().contains( "1.0.1" ) );
        
        Assert.assertEquals( 2, metadata.getVersioning().getVersions().size() );
    }

    private Date getLastDeployTimeStamp( Metadata metadata )
        throws ParseException
    {
        String lastUpdateString = metadata.getVersioning().getLastUpdated();

        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyyMMddHHmmss" );
        return dateFormat.parse( lastUpdateString );
    }

    private void deployWithMavenExpectSuccess( File mavenProject )
        throws VerificationException,
            IOException
    {
        // deploy using maven
        Verifier verifier = this.createVerifier( mavenProject );
        try
        {
            verifier.executeGoal( "deploy" );
            verifier.verifyErrorFreeLog();
        }

        catch ( VerificationException e )
        {
            failTest( verifier );
        }

    }
    
    private void deployWithMavenExpectFailure( File mavenProject )
    throws VerificationException,
        IOException
{
    // deploy using maven
    Verifier verifier = this.createVerifier( mavenProject );
    try
    {
        verifier.executeGoal( "deploy" );

        verifier.verifyErrorFreeLog();

        Assert.fail( "Should return 401 error" );
    }
    catch ( VerificationException e )
    {
        // expect error
    }

}

}
