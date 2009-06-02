package org.sonatype.nexus.integrationtests.nexus1646;

import java.io.File;
import java.io.FileInputStream;

import org.apache.maven.mercury.repository.metadata.Metadata;
import org.apache.maven.mercury.repository.metadata.MetadataBuilder;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.GavUtil;

import com.thoughtworks.xstream.XStream;

public class Nexus1646DeployArtifactsTest
    extends AbstractNexusIntegrationTest
{

    @Test
    public void deployPlainArtifact()
        throws Exception
    {
        Gav gav = GavUtil.newGav( "nexus1646", "artifact", "1.33.44" );
        File artifact = getTestFile( "artifact.jar" );

        int code = DeployUtils.deployUsingGavWithRest( REPO_TEST_HARNESS_RELEASE_REPO, gav, artifact );
        Assert.assertTrue( "Unable to deploy artifact " + code, Status.isSuccess( code ) );

        File metadataFile =
            new File( nexusWorkDir, "storage/nexus-test-harness-release-repo/nexus1646/artifact/maven-metadata.xml" );
        Assert.assertTrue( "Metadata file not found " + metadataFile.getAbsolutePath(), metadataFile.isFile() );

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
        int code = DeployUtils.deployUsingGavWithRest( REPO_TEST_HARNESS_RELEASE_REPO, gav, artifact );

        Assert.assertEquals( "Unable to deploy artifact " + code, 400, code );
    }

    @Test
    public void deployPlainSnapshotArtifact()
        throws Exception
    {
        Gav gav = GavUtil.newGav( "nexus1646", "artifact", "1.1.1-SNAPSHOT" );

        File artifact = getTestFile( "artifact.jar" );
        int code = DeployUtils.deployUsingGavWithRest( REPO_TEST_HARNESS_SNAPSHOT_REPO, gav, artifact );

        Assert.assertEquals( "Unable to deploy artifact " + code, 400, code );
    }

    @Test
    public void deployPluginArtifactUsingRest()
        throws Exception
    {
        File artifact = getTestFile( "changelog-maven-plugin-2.0-beta-1.jar" );
        File pom = getTestFile( "changelog-maven-plugin-2.0-beta-1.pom" );

        int code = DeployUtils.deployUsingPomWithRest( REPO_TEST_HARNESS_RELEASE_REPO, artifact, pom, null, null );
        Assert.assertTrue( "Unable to deploy artifact " + code, Status.isSuccess( code ) );

        // validate group metadata
        File metadataFile =
            new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_RELEASE_REPO
                + "/org/codehaus/mojo/maven-metadata.xml" );
        Assert.assertTrue( "Metadata file not found " + metadataFile.getAbsolutePath(), metadataFile.isFile() );
        
        // validate artifact metadata
        metadataFile =
            new File( nexusWorkDir, "storage/" + REPO_TEST_HARNESS_RELEASE_REPO
                + "/org/codehaus/mojo/changelog-maven-plugin/maven-metadata.xml" );
        Assert.assertTrue( "Metadata file not found " + metadataFile.getAbsolutePath(), metadataFile.isFile() );

        FileInputStream input = new FileInputStream( metadataFile );
        Metadata md = MetadataBuilder.read( input );
        IOUtil.close( input );

        System.out.println( new XStream().toXML( md ) );

        Assert.assertEquals( "2.0-beta-1", md.getVersioning().getLatest() );
        Assert.assertEquals( "2.0-beta-1", md.getVersioning().getRelease() );
        Assert.assertEquals( 1, md.getVersioning().getVersions().size() );
        Assert.assertEquals( "2.0-beta-1", md.getVersioning().getVersions().get( 0 ) );
    }

}
