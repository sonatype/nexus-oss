package org.sonatype.nexus.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.FileTestingUtils;

public class MultipleDeployTest
    extends AbstractNexusIntegrationTest
{

    public MultipleDeployTest()
    {
        super( "http://localhost:8081/nexus/content/groups/nexus-test/" );
    }

    private String getRepositoryURL( String repository )
    {
        return "http://localhost:8081/nexus/content/repositories/" + repository;
    }

    @Test
    public void singleDeployTest()
        throws Exception
    {
        // file to deploy
        File fileToDeploy = FileTestingUtils.getTestFile( this.getClass(), "singleDeployTest.xml" );

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryURL( "nexus-test-harness-repo" ),
                                     fileToDeploy, "org/sonatype/nexus-integration-tests/multiple-deploy-test/singleDeployTest/1/singleDeployTest-1.xml" );

        // download it
        File artifact = downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test", "singleDeployTest", "1", "xml", "./target/downloaded-jars" );

        // make sure its here
        assertTrue( artifact.exists() );

        // make sure it is what we expect.
        assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy, artifact ) );

        this.complete();
    }

    @Test
    public void deploySameFileMultipleTimesTest()
        throws Exception
    {
        // file to deploy
        File fileToDeploy = FileTestingUtils.getTestFile( this.getClass(), "deploySameFileMultipleTimesTest.xml" );

        String deployPath = "org/sonatype/nexus-integration-tests/multiple-deploy-test/deploySameFileMultipleTimesTest/1/deploySameFileMultipleTimesTest-1.xml";

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryURL( "nexus-test-harness-repo" ),
                                     fileToDeploy, deployPath );

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryURL( "nexus-test-harness-repo" ),
                                     fileToDeploy, deployPath );
        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryURL( "nexus-test-harness-repo" ),
                                     fileToDeploy, deployPath );

        // download it
        File artifact = downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test", "deploySameFileMultipleTimesTest", "1", "xml", "./target/downloaded-jars" );

        // make sure its here
        assertTrue( artifact.exists() );

        // make sure it is what we expect.
        assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy, artifact ) );

        this.complete();
    }

    @Test
    public void deployChangedFileMultipleTimesTest()
        throws Exception
    {
        // files to deploy
        File fileToDeploy1 = FileTestingUtils.getTestFile( this.getClass(), "deployChangedFileMultipleTimesTest1.xml" );
        File fileToDeploy2 = FileTestingUtils.getTestFile( this.getClass(), "deployChangedFileMultipleTimesTest2.xml" );
        File fileToDeploy3 = FileTestingUtils.getTestFile( this.getClass(), "deployChangedFileMultipleTimesTest3.xml" );

        String deployPath = "org/sonatype/nexus-integration-tests/multiple-deploy-test/deployChangedFileMultipleTimesTest/1/deployChangedFileMultipleTimesTest-1.xml";

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryURL( "nexus-test-harness-repo" ),
                                     fileToDeploy1, deployPath );

        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryURL( "nexus-test-harness-repo" ),
                                     fileToDeploy2, deployPath );
        // deploy it
        DeployUtils.deployWithWagon( this.getContainer(), "http", this.getRepositoryURL( "nexus-test-harness-repo" ),
                                     fileToDeploy3, deployPath );

        // download it
        File artifact = downloadArtifact( "org.sonatype.nexus-integration-tests.multiple-deploy-test", "deployChangedFileMultipleTimesTest", "1", "xml", "./target/downloaded-jars" );

        // make sure its here
        assertTrue( artifact.exists() );

        // make sure it is what we expect.
        assertTrue( FileTestingUtils.compareFileSHA1s( fileToDeploy3, artifact ) );

        // this should pass if the above passed
        assertFalse( FileTestingUtils.compareFileSHA1s( fileToDeploy2, artifact ) );

        this.complete();
    }

}

