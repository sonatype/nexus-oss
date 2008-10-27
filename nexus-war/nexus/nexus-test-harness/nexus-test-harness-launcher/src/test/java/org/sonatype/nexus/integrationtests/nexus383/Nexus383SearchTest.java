package org.sonatype.nexus.integrationtests.nexus383;

import java.io.File;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;

/**
 * Test Search operations.
 */
public class Nexus383SearchTest
    extends AbstractNexusIntegrationTest
{

    private static final String NEXUS_TEST_HARNESS_RELEASE_REPO = "nexus-test-harness-release-repo";

    private static final String NEXUS_TEST_HARNESS_REPO2 = "nexus-test-harness-repo2";

    private static final String NEXUS_TEST_HARNESS_REPO = "nexus-test-harness-repo";

    protected SearchMessageUtil messageUtil;

    public Nexus383SearchTest()
    {
        this.messageUtil = new SearchMessageUtil();
    }

    @Test
    // 1. deploy an known artifact. and search for it. using group Id and artifact Id.
    public void searchFor()
        throws Exception
    {

        // groupId
        List<NexusArtifact> results = messageUtil.searchFor( "nexus383" );
        Assert.assertEquals( 2, results.size() );

        // 3. negative test
        results = messageUtil.searchFor( "nexus-383" );
        Assert.assertTrue( results.isEmpty() );

        // artifactId
        results = messageUtil.searchFor( "know-artifact-1" );
        Assert.assertEquals( 1, results.size() );

        // artifactId
        results = messageUtil.searchFor( "know-artifact-2" );
        Assert.assertEquals( 1, results.size() );

        // partial artifactId
        results = messageUtil.searchFor( "know-artifact" );
        Assert.assertEquals( 2, results.size() );

        // 3. negative test
        results = messageUtil.searchFor( "unknow-artifacts" );
        Assert.assertTrue( results.isEmpty() );
    }

    @Test
    // 2. search using SHA1
    public void searchForSHA1()
        throws Exception
    {
        // know-artifact-1
        NexusArtifact result = messageUtil.searchForSHA1( "2e4213cd44e95dd306a74ba002ed1fa1282f0a51" );
        Assert.assertNotNull( result );

        // know-artifact-2
        result = messageUtil.searchForSHA1( "807f665cd73a2e62e169453e5af4cd5241b9a232" );
        Assert.assertNotNull( result );

        // velo's picture
        result = messageUtil.searchForSHA1( "612c17de73fdc8b9e3f6a063154d89946eb7c6f2" );
        Assert.assertNull( result );
    }

    @Test
    // 5. disable searching on a repo and do a search
    public void disableSearching()
        throws Exception
    {

        // Disabling default repo
        messageUtil.allowSearch( NEXUS_TEST_HARNESS_REPO, false );

        // groupId
        List<NexusArtifact> results = messageUtil.searchFor( "nexus383" );
        Assert.assertTrue( results.isEmpty() );

        // artifactId
        results = messageUtil.searchFor( "know-artifact-1" );
        Assert.assertTrue( results.isEmpty() );

        // artifactId
        results = messageUtil.searchFor( "know-artifact-2" );
        Assert.assertTrue( results.isEmpty() );

        // partial artifactId
        results = messageUtil.searchFor( "know-artifact" );
        Assert.assertTrue( results.isEmpty() );

    }

    @Test
    // 6. disable/enable searching on a repo and do a search
    public void disableEnableSearching()
        throws Exception
    {

        // Run disable mode first
        disableSearching();

        // Enabling default repo again
        messageUtil.allowSearch( NEXUS_TEST_HARNESS_REPO, true );

        // All searchs should run ok
        searchFor();
    }

    @Test
    // 7. make a repo not browseable and do a search
    public void disableBrowsing()
        throws Exception
    {
        // Enabling default repo again
        messageUtil.allowBrowsing( NEXUS_TEST_HARNESS_REPO, false );

        // All searchs should run ok
        searchFor();
    }

    @Test
    // 8. make a repo not browseable / browseable and do a search
    public void disableEnableBrowsing()
        throws Exception
    {

        // Run disable mode first
        disableBrowsing();

        // Enabling default repo again
        messageUtil.allowBrowsing( NEXUS_TEST_HARNESS_REPO, true );

        // All searchs should run ok
        searchFor();

    }

    @Test
    public void disableDeploying()
        throws Exception
    {
        // Enabling default repo again
        messageUtil.allowDeploying( NEXUS_TEST_HARNESS_REPO, false );

        // All searchs should run ok
        searchFor();
    }

    @Test
    // 4. deploy same artifact to multiple repos, and search
    public void crossRepositorySearch()
        throws Exception
    {
        Gav gav =
            new Gav( this.getTestId(), "crossArtifact", "1.0.0", null, "jar", 0, new Date().getTime(),
                     "A Cross Repository Deploy Artifact", false, false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + ".jar" );
        File pomFile = this.getTestFile( gav.getArtifactId() + ".pom" );

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        // Multi repository deploy
        DeployUtils.deployUsingPomWithRest( uploadURL, NEXUS_TEST_HARNESS_REPO, fileToDeploy, pomFile, null, null );
        DeployUtils.deployUsingPomWithRest( uploadURL, NEXUS_TEST_HARNESS_REPO2, fileToDeploy, pomFile, null, null );
        DeployUtils.deployUsingPomWithRest( uploadURL, NEXUS_TEST_HARNESS_RELEASE_REPO, fileToDeploy, pomFile, null, null );

        // if you deploy the same item multiple times to the same repo, that is only a single item
        DeployUtils.deployUsingPomWithRest( uploadURL, NEXUS_TEST_HARNESS_RELEASE_REPO, fileToDeploy, pomFile, null, null );
        DeployUtils.deployUsingPomWithRest( uploadURL, NEXUS_TEST_HARNESS_RELEASE_REPO, fileToDeploy, pomFile, null, null );

        RepositoryMessageUtil.updateIndexes( NEXUS_TEST_HARNESS_REPO, NEXUS_TEST_HARNESS_REPO2,
                                        NEXUS_TEST_HARNESS_RELEASE_REPO );

        List<NexusArtifact> results = messageUtil.searchFor( "crossArtifact" );
        Assert.assertEquals( 3, results.size() );

    }

    @BeforeClass
    public static void cleanWorkFolder() throws Exception {
        cleanWorkDir();
    }

}
