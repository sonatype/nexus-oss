/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.index.artifact.Gav;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.task.ArtifactoryMigrationTaskDescriptor;
import org.sonatype.nexus.plugins.migration.util.ImportMessageUtil;
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
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

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
        this.repositoryUtil = new RepositoryMessageUtil( this, getXMLXStream(), MediaType.APPLICATION_XML );
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

    @SuppressWarnings( "unchecked" )
    protected <E> void assertContains( ArrayList<E> collection, E item )
    {
        assertThat( item + " not found.\n" + collection, collection, hasItem( item ) );
    }

    @AfterMethod
    public void waitEnd()
        throws Exception
    {
        TestContainer.getInstance().getTestContext().useAdminForRequests();

        TaskScheduleUtil.waitForAllTasksToStop( ArtifactoryMigrationTaskDescriptor.ID );

        Thread.sleep( 2000 );

        String log = FileUtils.readFileToString( migrationLogFile );
        assertThat( log, log.toLowerCase(), not( containsString( "Exception".toLowerCase() ) ) );
        assertThat( log, log.toLowerCase(), not( containsString( "Error".toLowerCase() ) ) );

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

        assertThat( "Downloaded artifact was not right, checksum comparation fail " + artifactId,
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
        assertThat( "Expected to found only one artifact (" + artifactId + ") instead of " + artifacts.size() + "\n"
            + this.getXMLXStream().toXML( artifacts ), artifacts.size(), is( equalTo( 1 ) ) );
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
        assertThat( "Unexpected result from server: " + migrationSummary, migrationSummary, is( notNullValue() ) );
        return migrationSummary;
    }

    protected void commitMigration( MigrationSummaryDTO migrationSummary )
        throws Exception
    {
        Status status = ImportMessageUtil.commitImport( migrationSummary ).getStatus();
        assertThat( "Unable to commit import " + status, status.isSuccess() );

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

    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();
        copyConfigFile( "logback-migration.xml", getTestProperties(), WORK_CONF_DIR );
        copyConfigFile( "artifactory-bridge/WEB-INF/classes/logback.xml", getTestProperties(), nexusBaseDir );
        copyConfigFile( "artifactory-bridge/WEB-INF/plexus.properties", getTestProperties(), nexusBaseDir );
    }

}
