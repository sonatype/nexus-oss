/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.integrationtests.nexus3233;

import static org.sonatype.nexus.integrationtests.ITGroups.INDEX;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpMethod;
import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.util.FileUtils;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.maven.tasks.descriptors.RebuildMavenMetadataTaskDescriptor;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.UpdateIndexTask;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GavUtil;
import org.sonatype.nexus.test.utils.MavenDeployer;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Nexus3233IndexPomSha1IT
    extends AbstractNexusIntegrationTest
{
    @Test(groups = INDEX)
    public void wagonDeploy()
        throws Exception
    {
        final File pom = getTestFile( "wagon.pom" );
        final File sha1 = new File( pom.getParentFile(), "wagon.pom.sha1" );
        FileUtils.fileWrite( sha1.getAbsolutePath(), FileTestingUtils.createSHA1FromFile( pom ) );

        final String repo = getRepositoryUrl( REPO_TEST_HARNESS_REPO );
        final Gav gav = GavUtil.newGav( "nexus3233", "wagon", "1.0.0", "pom" );
        final String path = getRelitiveArtifactPath( gav );
        getDeployUtils().deployWithWagon( "http", repo, pom, path );
        getDeployUtils().deployWithWagon( "http", repo, sha1, path + ".sha1" );
        searchFor( pom );
    }

    @Test(groups = INDEX)
    public void mavenDeploy()
        throws Exception
    {
        final File pom = getTestFile( "maven.pom" );
        MavenDeployer.deployAndGetVerifier( GavUtil.newGav( "nexus3233", "maven", "1.0.0", "pom" ),
            getRepositoryUrl( REPO_TEST_HARNESS_REPO ), pom, null, "-DgeneratePom=false" ).verifyErrorFreeLog();
        searchFor( pom );
    }

    @Test(groups = INDEX)
    public void restDeploy()
        throws Exception
    {
        final File pom = getTestFile( "rest.pom" );
        HttpMethod r = getDeployUtils().deployPomWithRest( REPO_TEST_HARNESS_REPO, pom );
        Assert.assertTrue( Status.isSuccess( r.getStatusCode() ),
        "Unable to deploy artifact " + r.getStatusCode() + ": " + r.getStatusText() );
        searchFor( pom );
    }

    @Test(groups = INDEX)
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

        repo.setKey( "repositoryId" );

        repo.setValue(  REPO_TEST_HARNESS_REPO );

        TaskScheduleUtil.runTask( "RebuildMavenMetadata-Nexus3233", RebuildMavenMetadataTaskDescriptor.ID, repo );
        
        RepositoryMessageUtil.updateIndexes( REPO_TEST_HARNESS_REPO );
        TaskScheduleUtil.waitForAllTasksToStop();
        doSearch( sha1, "after reindexing!" );
    }

    private void searchFor( final File pom )
        throws IOException, Exception
    {
        // wait to index up the changes
        getEventInspectorsUtil().waitForCalmPeriod();

        String sha1 = FileTestingUtils.createSHA1FromFile( pom );
        Assert.assertNotNull( sha1 );
        doSearch( sha1, "" );

        RepositoryMessageUtil.updateIndexes( REPO_TEST_HARNESS_REPO );
        TaskScheduleUtil.waitForAllTasksToStop( UpdateIndexTask.class );
        doSearch( sha1, "after reindexing!" );
    }

    private void doSearch( String sha1, String msg )
        throws Exception
    {
        // wait to index up the changes
        getEventInspectorsUtil().waitForCalmPeriod();

        NexusArtifact result = getSearchMessageUtil().identify( sha1 );
        Assert.assertNotNull( result, "Pom with " + sha1 + " not found " + msg );
    }
}
