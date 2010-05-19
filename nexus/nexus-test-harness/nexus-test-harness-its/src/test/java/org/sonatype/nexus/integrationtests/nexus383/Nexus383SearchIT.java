/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.integrationtests.nexus383;

import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.junit.After;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.index.SearchType;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

/**
 * Test Search operations.
 */
public class Nexus383SearchIT
    extends AbstractNexusIntegrationTest
{

    private static final String NEXUS_TEST_HARNESS_RELEASE_REPO = "nexus-test-harness-release-repo";

    private static final String NEXUS_TEST_HARNESS_REPO2 = "nexus-test-harness-repo2";

    private static final String NEXUS_TEST_HARNESS_REPO = "nexus-test-harness-repo";

    protected GroupMessageUtil groupMessageUtil;

    public Nexus383SearchIT()
    {
        this.groupMessageUtil = new GroupMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
    }

    @Override
    protected void deployArtifacts()
        throws Exception
    {
        RepositoryMessageUtil.updateIndexes( NEXUS_TEST_HARNESS_RELEASE_REPO, NEXUS_TEST_HARNESS_REPO2,
            NEXUS_TEST_HARNESS_REPO );

        TaskScheduleUtil.waitForAllTasksToStop();

        super.deployArtifacts();
    }

    @After
    public void resetRepo()
        throws Exception
    {
        getSearchMessageUtil().allowSearch( NEXUS_TEST_HARNESS_REPO, true );
        getSearchMessageUtil().allowBrowsing( NEXUS_TEST_HARNESS_REPO, true );
        getSearchMessageUtil().allowDeploying( NEXUS_TEST_HARNESS_REPO, true );
    }

    @Test
    // 1. deploy an known artifact. and search for it. using group Id and artifact Id.
    public void searchFor()
        throws Exception
    {
        TaskScheduleUtil.waitForAllTasksToStop();

        // groupId
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "nexus383" );
        Assert.assertEquals( 2, results.size() );

        // 3. negative test
        results = getSearchMessageUtil().searchFor( "nexus-383" );
        Assert.assertTrue( results.isEmpty() );

        // artifactId
        results = getSearchMessageUtil().searchFor( "know-artifact-1", SearchType.EXACT );
        Assert.assertEquals( 1, results.size() );

        // artifactId
        results = getSearchMessageUtil().searchFor( "know-artifact-2", SearchType.EXACT );
        Assert.assertEquals( 1, results.size() );

        // partial artifactId
        results = getSearchMessageUtil().searchFor( "know-artifact" );
        Assert.assertEquals( 2, results.size() );

        // 3. negative test
        results = getSearchMessageUtil().searchFor( "unknow-artifacts" );
        Assert.assertTrue( results.isEmpty() );

        // NEXUS-2724: the member changes should propagate to it's groups too
        // has it propagated to group?
        results = getSearchMessageUtil().searchForGav( "nexus383", "know-artifact-1", "1.0.0", "public" );
        Assert.assertEquals( 1, results.size() );
        results = getSearchMessageUtil().searchForGav( "nexus383", "know-artifact-2", "1.0.0", "public" );
        Assert.assertEquals( 1, results.size() );
    }

    @Test
    // 2. search using SHA1
    public void searchForSHA1()
        throws Exception
    {
        // know-artifact-1
        NexusArtifact result = getSearchMessageUtil().identify( "4ce1d96bd11b8959b32a75c1fa5b738d7b87d408" );
        Assert.assertNotNull( result );

        // know-artifact-2
        result = getSearchMessageUtil().identify( "230377663ac3b19ad83c99b0afdb056dd580c5c8" );
        Assert.assertNotNull( result );

        // velo's picture
        result = getSearchMessageUtil().identify( "612c17de73fdc8b9e3f6a063154d89946eb7c6f2" );
        Assert.assertNull( result );
    }

    @Test
    // 5. disable searching on a repo and do a search
    public void disableSearching()
        throws Exception
    {

        // Disabling default repo
        getSearchMessageUtil().allowSearch( NEXUS_TEST_HARNESS_REPO, false );

        // groupId
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "nexus383" );
        Assert.assertTrue( results.isEmpty() );

        // artifactId
        results = getSearchMessageUtil().searchFor( "know-artifact-1" );
        Assert.assertTrue( results.isEmpty() );

        // artifactId
        results = getSearchMessageUtil().searchFor( "know-artifact-2" );
        Assert.assertTrue( results.isEmpty() );

        // partial artifactId
        results = getSearchMessageUtil().searchFor( "know-artifact" );
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
        getSearchMessageUtil().allowSearch( NEXUS_TEST_HARNESS_REPO, true );

        // All searchs should run ok
        searchFor();
    }

    @Test
    // 7. make a repo not browseable and do a search
    public void disableBrowsing()
        throws Exception
    {
        // Enabling default repo again
        getSearchMessageUtil().allowBrowsing( NEXUS_TEST_HARNESS_REPO, false );

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
        getSearchMessageUtil().allowBrowsing( NEXUS_TEST_HARNESS_REPO, true );

        // All searchs should run ok
        searchFor();

    }

    @Test
    public void disableDeploying()
        throws Exception
    {
        // Enabling default repo again
        getSearchMessageUtil().allowDeploying( NEXUS_TEST_HARNESS_REPO, false );

        // All searchs should run ok
        searchFor();

        getSearchMessageUtil().allowDeploying( NEXUS_TEST_HARNESS_REPO, true );
    }

    @Test
    // 4. deploy same artifact to multiple repos, and search
    public void crossRepositorySearch()
        throws Exception
    {
        // file to deploy
        File fileToDeploy = this.getTestFile( "crossArtifact.jar" );
        File pomFile = this.getTestFile( "crossArtifact.pom" );

        MavenXpp3Reader reader = new MavenXpp3Reader();
        Model model = reader.read( new FileReader( pomFile ) );

        String deployUrl = model.getDistributionManagement().getRepository().getUrl();

        Gav gav =
            new Gav( model.getGroupId(), model.getArtifactId(), model.getVersion(), null, model.getPackaging(), 0,
                new Date().getTime(), model.getName(), false, false, null, false, null );

        // Multi repository deploy
        getDeployUtils().deployWithWagon( "http", deployUrl, fileToDeploy, this.getRelitiveArtifactPath( gav ) );
        getDeployUtils().deployWithWagon( "http",
            deployUrl.replace( NEXUS_TEST_HARNESS_REPO, NEXUS_TEST_HARNESS_REPO2 ), fileToDeploy,
            this.getRelitiveArtifactPath( gav ) );
        getDeployUtils().deployWithWagon( "http",
            deployUrl.replace( NEXUS_TEST_HARNESS_REPO, NEXUS_TEST_HARNESS_RELEASE_REPO ), fileToDeploy,
            this.getRelitiveArtifactPath( gav ) );
        getDeployUtils().deployWithWagon( "http", deployUrl, pomFile, this.getRelitivePomPath( gav ) );
        getDeployUtils().deployWithWagon( "http",
            deployUrl.replace( NEXUS_TEST_HARNESS_REPO, NEXUS_TEST_HARNESS_REPO2 ), pomFile,
            this.getRelitivePomPath( gav ) );
        getDeployUtils().deployWithWagon( "http",
            deployUrl.replace( NEXUS_TEST_HARNESS_REPO, NEXUS_TEST_HARNESS_RELEASE_REPO ), pomFile,
            this.getRelitivePomPath( gav ) );

        // if you deploy the same item multiple times to the same repo, that is only a single item
        getDeployUtils().deployWithWagon( "http",
            deployUrl.replace( NEXUS_TEST_HARNESS_REPO, NEXUS_TEST_HARNESS_RELEASE_REPO ), fileToDeploy,
            this.getRelitiveArtifactPath( gav ) );
        getDeployUtils().deployWithWagon( "http",
            deployUrl.replace( NEXUS_TEST_HARNESS_REPO, NEXUS_TEST_HARNESS_RELEASE_REPO ), pomFile,
            this.getRelitivePomPath( gav ) );
        getDeployUtils().deployWithWagon( "http",
            deployUrl.replace( NEXUS_TEST_HARNESS_REPO, NEXUS_TEST_HARNESS_RELEASE_REPO ), fileToDeploy,
            this.getRelitiveArtifactPath( gav ) );
        getDeployUtils().deployWithWagon( "http",
            deployUrl.replace( NEXUS_TEST_HARNESS_REPO, NEXUS_TEST_HARNESS_RELEASE_REPO ), pomFile,
            this.getRelitivePomPath( gav ) );

        RepositoryMessageUtil.updateIndexes( NEXUS_TEST_HARNESS_REPO, NEXUS_TEST_HARNESS_REPO2,
            NEXUS_TEST_HARNESS_RELEASE_REPO );

        TaskScheduleUtil.waitForTasks();

        // Keyword search does collapse results, so we need _1_
        List<NexusArtifact> results = getSearchMessageUtil().searchFor( "crossArtifact" );
        Assert.assertEquals( 1, results.size() );

        // GAV search does not
        results = getSearchMessageUtil().searchForGav( gav.getGroupId(), gav.getArtifactId(), gav.getVersion() );
        Assert.assertEquals( 3, results.size() );

    }

}
