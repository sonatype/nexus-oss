package org.sonatype.nexus.integrationtests.nexus1646;

import java.io.File;
import java.io.FileInputStream;

import org.apache.log4j.Logger;
import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.codehaus.plexus.util.IOUtil;
import org.restlet.data.Status;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.GavUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.thoughtworks.xstream.XStream;

public class Nexus1646DeployArtifactsIT
    extends AbstractNexusIntegrationTest
{
    protected static Logger logger = Logger.getLogger( Nexus1646DeployArtifactsIT.class );

    @Test
    public void deployPlainArtifact()
        throws Exception
    {
        Gav gav = GavUtil.newGav( "nexus1646", "artifact", "1.33.44" );
        File artifact = getTestFile( "artifact.jar" );

        int code = getDeployUtils().deployUsingGavWithRest( REPO_TEST_HARNESS_RELEASE_REPO, gav, artifact );
        Assert.assertTrue( Status.isSuccess( code ), "Unable to deploy artifact " + code );

        File metadataFile =
            new File( nexusWorkDir, "storage/nexus-test-harness-release-repo/nexus1646/artifact/maven-metadata.xml" );
        Assert.assertTrue( metadataFile.isFile(), "Metadata file not found " + metadataFile.getAbsolutePath() );

        FileInputStream input = new FileInputStream( metadataFile );
        Metadata md = MetadataBuilder.read( input );
        IOUtil.close( input );

        Assert.assertEquals( gav.getVersion(), md.getVersioning().getLatest() );
        Assert.assertEquals( gav.getVersion(), md.getVersioning().getRelease() );
        Assert.assertEquals( 1, md.getVersioning().getVersions().size() );
        Assert.assertEquals( gav.getVersion(), md.getVersioning().getVersions().get( 0 ) );
    }

    @Test
    public void deploySnapshotToRelease()
        throws Exception
    {
        Gav gav = GavUtil.newGav( "nexus1646", "artifact", "1.1.1-SNAPSHOT" );

        File artifact = getTestFile( "artifact.jar" );
        int code = getDeployUtils().deployUsingGavWithRest( REPO_TEST_HARNESS_RELEASE_REPO, gav, artifact );

        Assert.assertEquals( code, 400, "Unable to deploy artifact " + code );
    }

    @Test
    public void deployPlainSnapshotArtifact()
        throws Exception
    {
        Gav gav = GavUtil.newGav( "nexus1646", "artifact", "1.1.1-SNAPSHOT" );

        File artifact = getTestFile( "artifact.jar" );
        int code = getDeployUtils().deployUsingGavWithRest( REPO_TEST_HARNESS_SNAPSHOT_REPO, gav, artifact );

        Assert.assertEquals( code, 400, "Unable to deploy artifact " + code );
    }

    @Test
    public void deployPluginArtifactUsingRest()
        throws Exception
    {
        File artifact = getTestFile( "changelog-maven-plugin-2.0-beta-1.jar" );
        File pom = getTestFile( "changelog-maven-plugin-2.0-beta-1.pom" );

        int code = getDeployUtils().deployUsingPomWithRest( REPO_TEST_HARNESS_RELEASE_REPO, artifact, pom, null, null );
        Assert.assertTrue( Status.isSuccess( code ), "Unable to deploy artifact " + code );

        // validate group metadata
        File metadataFile =
            new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_RELEASE_REPO
                + "/org/codehaus/mojo/maven-metadata.xml" );
        Assert.assertTrue( metadataFile.isFile(), "Metadata file not found " + metadataFile.getAbsolutePath() );

        // validate artifact metadata
        metadataFile =
            new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_RELEASE_REPO
                + "/org/codehaus/mojo/changelog-maven-plugin/maven-metadata.xml" );
        Assert.assertTrue( metadataFile.isFile(), "Metadata file not found " + metadataFile.getAbsolutePath() );

        FileInputStream input = new FileInputStream( metadataFile );
        Metadata md = MetadataBuilder.read( input );
        IOUtil.close( input );

        logger.info( new XStream().toXML( md ) );

        Assert.assertEquals( "2.0-beta-1", md.getVersioning().getLatest() );
        Assert.assertEquals( "2.0-beta-1", md.getVersioning().getRelease() );
        Assert.assertEquals( 1, md.getVersioning().getVersions().size() );
        Assert.assertEquals( "2.0-beta-1", md.getVersioning().getVersions().get( 0 ) );
    }

}
