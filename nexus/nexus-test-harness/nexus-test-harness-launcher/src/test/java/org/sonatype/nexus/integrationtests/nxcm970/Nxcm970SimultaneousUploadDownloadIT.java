package org.sonatype.nexus.integrationtests.nxcm970;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;

public class Nxcm970SimultaneousUploadDownloadIT
    extends AbstractNexusIntegrationTest
{
    private Executor executor = Executors.newSingleThreadExecutor();

    @Test
    public void testSimultaneousUploadDownload()
        throws Exception
    {
        // preparations
        String baseUrl = getRepositoryUrl( "nexus-test-harness-repo" );
        // add path
        String targetUrl = baseUrl + "nxcm970/artifact/1.0/artifact-1.0.pom";

        // create deployer that we will control how long to "deploy"
        ContinuousDeployer continuousDeployer = new ContinuousDeployer( targetUrl );

        // download the subjectArtifact -- should result in 404
        // downloadSubjectArtifact( false, baseUrl );

        // start deploying the subjectArtifact -- should work on it
        executor.execute( continuousDeployer );

        // download the subjectArtifact -- should result in 404
        downloadSubjectArtifact( false, baseUrl );

        // let it work a lil'
        Thread.sleep( 1000 );

        // download the subjectArtifact -- should result in 404
        downloadSubjectArtifact( false, baseUrl );

        // let it work a lil'
        Thread.sleep( 1000 );

        // download the subjectArtifact -- should result in 404
        downloadSubjectArtifact( false, baseUrl );

        // finish deploying the subjectArtifaft -- should finish succesfully
        continuousDeployer.finishDeploying();

        // wait to finish the HTTP tx, check result
        while ( !continuousDeployer.isFinished() )
        {
            Thread.sleep( 200 );
        }

        Assert.assertTrue( "Deployment failed: " + continuousDeployer.getResult(),
                           continuousDeployer.getResult() == 201 );

        // download the subjectArtifact -- should result in 200, found
        downloadSubjectArtifact( true, baseUrl );
    }

    // ==

    protected void downloadSubjectArtifact( boolean shouldSucceed, String baseUrl )
    {
        try
        {
            downloadArtifact( baseUrl, "nxcm970", "artifact", "1.0", "pom", null, "./target/downloaded-jars" );

            if ( !shouldSucceed )
            {
                Assert.fail( "Should not succeed the retrieval!" );
            }
        }
        catch ( IOException e )
        {
            if ( shouldSucceed )
            {
                Assert.fail( "Should succeed the retrieval!" );
            }
        }
    }

}
