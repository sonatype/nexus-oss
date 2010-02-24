package org.sonatype.nexus.integrationtests.nexus3233;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.commons.httpclient.HttpMethod;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;
import org.restlet.data.Status;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.RebuildMavenMetadataTask;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.ReindexTask;
import org.sonatype.nexus.test.utils.DeployUtils;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.MavenDeployer;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class Nexus3233IndexPomSha1IT
    extends AbstractNexusIntegrationTest
{
    protected SearchMessageUtil messageUtil;

    public Nexus3233IndexPomSha1IT()
    {
        this.messageUtil = new SearchMessageUtil();
    }

    @Test
    public void wagonDeploy()
        throws Exception
    {
        final File pom = getTestFile( "wagon.pom" );
        final File sha1 = new File( pom.getParentFile(), "wagon.pom.sha1" );
        FileUtils.fileWrite( sha1.getAbsolutePath(), FileTestingUtils.createSHA1FromFile( pom ) );

        final String repo = getRepositoryUrl( REPO_TEST_HARNESS_REPO );
        final Gav gav = GavUtil.newGav( "nexus3233", "wagon", "1.0.0", "pom" );
        final String path = getRelitiveArtifactPath( gav );
        DeployUtils.deployWithWagon( container, "http", repo, pom, path );
        DeployUtils.deployWithWagon( container, "http", repo, sha1, path + ".sha1" );
        searchFor( pom );
    }

    @Test
    public void mavenDeploy()
        throws Exception
    {
        final File pom = getTestFile( "maven.pom" );
        MavenDeployer.deployAndGetVerifier( GavUtil.newGav( "nexus3233", "maven", "1.0.0", "pom" ),
                                            getRepositoryUrl( REPO_TEST_HARNESS_REPO ), pom, null, "-DgeneratePom=false" ).verifyErrorFreeLog();
        searchFor( pom );
    }

    @Test
    public void restDeploy()
        throws Exception
    {
        final File pom = getTestFile( "rest.pom" );
        HttpMethod r = DeployUtils.deployPomWithRest( REPO_TEST_HARNESS_REPO, pom );
        Assert.assertTrue( "Unable to deploy artifact " + r.getStatusCode() + ": " + r.getStatusText(),
                           Status.isSuccess( r.getStatusCode() ) );
        searchFor( pom );
    }

    @Test
    public void manualStorage()
        throws Exception
    {
        final File pom = getTestFile( "manual.pom" );
        File dest = new File( nexusWorkDir, "storage/nexus-test-harness-repo/nexus3233/manual/1.0.0/manual-1.0.0.pom" );
        dest.getParentFile().mkdirs();
        FileUtils.copyFile( pom, dest );

        String sha1 = FileTestingUtils.createSHA1FromFile( pom );
        Assert.assertNotNull( sha1 );

        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();

        repo.setId( "repositoryOrGroupId" );

        repo.setValue( "repo_" + REPO_TEST_HARNESS_REPO );

        ScheduledServiceListResource task =
            TaskScheduleUtil.runTask( "RebuildMavenMetadata-Nexus3233", RebuildMavenMetadataTaskDescriptor.ID, repo );
        Assert.assertNotNull( task );
        TaskScheduleUtil.waitForAllTasksToStop( RebuildMavenMetadataTask.class );

        RepositoryMessageUtil.updateIndexes( REPO_TEST_HARNESS_REPO );
        TaskScheduleUtil.waitForAllTasksToStop( ReindexTask.class );
        doSearch( sha1, "after reindexing!" );
    }

    private void searchFor( final File pom )
        throws IOException, Exception
    {
        String sha1 = FileTestingUtils.createSHA1FromFile( pom );
        Assert.assertNotNull( sha1 );
        doSearch( sha1, "" );

        RepositoryMessageUtil.updateIndexes( REPO_TEST_HARNESS_REPO );
        TaskScheduleUtil.waitForAllTasksToStop( ReindexTask.class );
        doSearch( sha1, "after reindexing!" );
    }

    private void doSearch( String sha1, String msg )
        throws Exception
    {
        NexusArtifact result = messageUtil.searchForSHA1( sha1 );
        Assert.assertNotNull( "Pom with " + sha1 + " not found " + msg, result );
    }
}
