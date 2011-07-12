/**
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.migration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.index.artifact.Gav;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.task.ArtifactoryMigrationTaskDescriptor;
import org.sonatype.nexus.plugins.migration.util.ImportMessageUtil;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryGroupMemberRepository;
import org.sonatype.nexus.rest.model.RepositoryGroupResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.test.utils.EventInspectorsUtil;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.RoleMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;
import org.sonatype.nexus.test.utils.UserMessageUtil;

public abstract class AbstractMigrationIntegrationTest
    extends AbstractNexusIntegrationTest
{

    protected File migrationLogFile;

    protected RepositoryMessageUtil repositoryUtil;

    protected GroupMessageUtil groupUtil;

    protected SearchMessageUtil searchUtil;

    protected UserMessageUtil userUtil;

    protected RoleMessageUtil roleUtil;

    public AbstractMigrationIntegrationTest()
    {
        this.repositoryUtil =
            new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
        this.groupUtil = new GroupMessageUtil( this, this.getXMLXStream(), MediaType.APPLICATION_XML );
        this.searchUtil = new SearchMessageUtil( this );
        this.userUtil = new UserMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
        this.roleUtil = new RoleMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );

        this.migrationLogFile = new File( nexusLogDir, getTestId() + "/migration.log" );

    }

    @BeforeClass
    public static void clean()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().setSecureTest( false );

        getNexusStatusUtil().stop();

        cleanWorkDir();
    }

    protected <E> void assertContains( ArrayList<E> collection, E item )
    {
        Assert.assertTrue( item + " not found.\n" + collection, collection.contains( item ) );
    }

    @After
    public void waitEnd()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        TaskScheduleUtil.waitForAllTasksToStop( ArtifactoryMigrationTaskDescriptor.ID );

        Thread.sleep( 2000 );

        String log = FileUtils.readFileToString( migrationLogFile );
        Assert.assertFalse( log, log.toLowerCase().contains( "Exception".toLowerCase() ) );
        Assert.assertFalse( log, log.toLowerCase().contains( "Error".toLowerCase() ) );

        TaskScheduleUtil.waitForAllTasksToStop();
    }

    protected void checkArtifact( String repositoryId, String groupId, String artifactId, String version )
        throws Exception
    {
        checkArtifact( repositoryId, groupId, artifactId, version, false );
    }

    protected void checkArtifact( String repositoryId, String groupId, String artifactId, String version,
                                  boolean isGroup )
        throws Exception
    {
        File artifact = getTestFile( "artifact.jar" );
        Gav gav =
            new Gav( groupId, artifactId, version, null, "jar", null, null, null, false, false, null, false, null );
        File downloaded;
        try
        {
            if ( isGroup )
            {
                downloaded = downloadArtifactFromGroup( repositoryId, gav, "target/downloads/" + groupId );
            }
            else
            {
                downloaded = downloadArtifactFromRepository( repositoryId, gav, "target/downloads/" + groupId );
            }
        }
        catch ( IOException e )
        {
            Assert.fail( "Unable to download artifact " + artifactId + " got:\n" + e.getMessage() );
            throw e; // never happen
        }

        Assert.assertTrue( "Downloaded artifact was not right, checksum comparation fail " + artifactId,
            FileTestingUtils.compareFileSHA1s( artifact, downloaded ) );
    }

    protected void checkArtifactNotPresent( String repositoryId, String groupId, String artifactId, String version )
        throws Exception
    {
        Gav gav =
            new Gav( groupId, artifactId, version, null, "jar", null, null, null, false, false, null, false, null );
        try
        {
            downloadArtifactFromRepository( repositoryId, gav, "target/downloads/" + groupId );
            Assert.fail( "Unable to download artifact " + artifactId );
        }
        catch ( FileNotFoundException e )
        {
            // expected
        }
    }

    protected void checkArtifactOnGroup( String nexusGroupId, String groupId, String artifactId, String version )
        throws Exception
    {
        checkArtifact( nexusGroupId, groupId, artifactId, version, true );
    }

    protected void checkIndex( String repoId, String groupId, String artifactId, String version )
        throws Exception
    {
        List<NexusArtifact> artifacts = searchUtil.searchForGav( groupId, artifactId, version, repoId );
        Assert.assertEquals( "Expected to found only one artifact (" + artifactId + ") instead of " + artifacts.size()
            + "\n" + this.getXMLXStream().toXML( artifacts ), 1, artifacts.size() );
    }

    protected void checkGroup( String groupId )
        throws IOException
    {
        ArrayList<String> groupsIds = new ArrayList<String>();
        List<RepositoryGroupListResource> groups = this.groupUtil.getList();
        for ( RepositoryGroupListResource group : groups )
        {
            groupsIds.add( group.getId() );
        }
        assertContains( groupsIds, groupId );

    }

    protected void checkSnapshotReleaseRepository( String repoId )
        throws IOException
    {
        RepositoryGroupResource g = this.groupUtil.getGroup( repoId );
        Assert.assertNotNull( g );

        String releaseId = repoId + "-releases";
        Assert.assertNotNull( this.repositoryUtil.getRepository( releaseId ) );
        String snapshotId = repoId + "-snapshots";
        Assert.assertNotNull( this.repositoryUtil.getRepository( snapshotId ) );

        ArrayList<String> reposIds = new ArrayList<String>();
        for ( RepositoryGroupMemberRepository repo : g.getRepositories() )
        {
            reposIds.add( repo.getId() );
        }
        assertContains( reposIds, releaseId );
        assertContains( reposIds, snapshotId );
    }

    protected void checkRepository( String repoId )
        throws IOException
    {
        ArrayList<String> reposIds = new ArrayList<String>();
        List<RepositoryListResource> repositories = this.repositoryUtil.getList();
        for ( RepositoryListResource repo : repositories )
        {
            reposIds.add( repo.getId() );
        }
        assertContains( reposIds, repoId );
    }

    protected MigrationSummaryDTO prepareMigration( File artifactoryBackup )
        throws IOException
    {
        MigrationSummaryDTO migrationSummary = ImportMessageUtil.importBackup( artifactoryBackup );
        Assert.assertNotNull( "Unexpected result from server: " + migrationSummary, migrationSummary );
        return migrationSummary;
    }

    protected void commitMigration( MigrationSummaryDTO migrationSummary )
        throws Exception
    {
        Status status = ImportMessageUtil.commitImport( migrationSummary ).getStatus();
        Assert.assertTrue( "Unable to commit import " + status, status.isSuccess() );

        waitForCompletion();
    }

    protected void checkNotAvailable( String repositoryId, String groupId, String artifactId, String version )
        throws Exception
    {
        Gav gav =
            new Gav( groupId, artifactId, version, null, "jar", null, null, null, false, false, null, false, null );
        try
        {
            downloadArtifactFromRepository( repositoryId, gav, "target/downloads/nxcm259" );
            Assert.fail( "Artifact available at wrong repository " + artifactId );
        }
        catch ( FileNotFoundException e )
        {
            // expected
        }

    }

    protected void waitForCompletion()
        throws Exception
    {
        TaskScheduleUtil.waitForAllTasksToStop();
        new EventInspectorsUtil( this ).waitForCalmPeriod();
        TaskScheduleUtil.waitForAllTasksToStop();
    }
    
}
