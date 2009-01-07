package org.sonatype.nexus.integrationtests.nexus1329;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.proxy.AbstractNexusProxyIntegrationTest;
import org.sonatype.nexus.test.utils.FileTestingUtils;

/**
 *
 * The nexus.xml for this test is located: /nexus-test-harness-launcher/src/test/resources/nexus1329/test-config/nexus.xml
 * 
 * The mirrors are located: /nexus-test-harness-launcher/src/test/resources/proxyRepo/nexus1329
 * They are located on the same server with different URLs.
 *
 */
public class Nexus1329MirrorTest
    extends AbstractNexusProxyIntegrationTest // AbstractNexusProxyIntegrationTest should be moved out of proxy package when build is stable
{

    @Test
    public void downloadFromMirrorTest()
        throws IOException
    {
        // add a mirror
        // TODO: add REST call

        // build a GAV for the artifact
        Gav gav = new Gav(
            this.getTestId(),
            "sample",
            "1.0.0",
            null,
            "xml",
            0,
            new Date().getTime(),
            "Artifact Name",
            false,
            false,
            null,
            false,
            null );

        String repositoryId = "nexus1329-repo";

        // download file from repo
        File artifactFile = this.downloadArtifactFromRepository( repositoryId, gav, "./target/downloadedFiles/repo" );
        Assert.assertNotNull( artifactFile );
        Assert.assertTrue( artifactFile.exists() );

        // if you want to check the downloaded file against a known file you can compare the SHA1's
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( this.getLocalFile(
            this.getTestId() + "/repository",
            gav ), artifactFile ) );

        // download file from group
        artifactFile = this.downloadArtifactFromGroup( "nexus1329-group", gav, "./target/downloadedFiles/group" );

    }

    @Test
    public void downloadFileThatIsOnlyInMirrorTest() throws IOException
    {
        // build a GAV for the artifact
        Gav gav = new Gav(
            this.getTestId(),
            "sample",
            "1.0.1", // Different version then above
            null,
            "xml",
            0,
            new Date().getTime(),
            "Artifact Name",
            false,
            false,
            null,
            false,
            null );

        String repositoryId = "nexus1329-repo";

        // download file from repo
        File artifactFile = this.downloadArtifactFromRepository( repositoryId, gav, "./target/downloadedFiles/repo" );
        Assert.assertNotNull( artifactFile );
        Assert.assertTrue( artifactFile.exists() );

        // if you want to check the downloaded file against a known file you can compare the SHA1's
        Assert.assertTrue( FileTestingUtils.compareFileSHA1s( this.getLocalFile(
            this.getTestId() + "/nexus1329-repo",
            gav ), artifactFile ) );

        // download file from group
        artifactFile = this.downloadArtifactFromGroup( "nexus1329-group", gav, "./target/downloadedFiles/group" );
    }

}
