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

import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.util.ImportMessageUtil;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public abstract class AbstractMigrationIntegrationTest
    extends AbstractNexusIntegrationTest
{

    protected RepositoryMessageUtil repositoryUtil;

    protected GroupMessageUtil groupUtil;

    protected SearchMessageUtil searchUtil;

    public AbstractMigrationIntegrationTest()
    {
        try
        {
            this.repositoryUtil = new RepositoryMessageUtil( getXMLXStream(), MediaType.APPLICATION_XML, this.getRepositoryTypeRegistry() );
        }
        catch ( ComponentLookupException e )
        {
            Assert.fail( "Failed to lookup component: "+ e.getMessage() );
        }
        this.groupUtil = new GroupMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
        this.searchUtil = new SearchMessageUtil();
    }

    @BeforeClass
    public static void clean()
    {
        TestContainer.getInstance().getTestContext().setSecureTest( false );

        try
        {
            cleanWorkDir();
        }
        catch ( IOException e )
        {
            // is not a good sign, but I can ignore it
            log.error( "Error deleting work dir", e );
        }
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

        TaskScheduleUtil.waitForTasks( 40 );

        Thread.sleep( 2000 );
    }

    protected void checkArtifact( String repositoryId, String groupId, String artifactId, String version )
        throws IOException
    {
        File artifact = getTestFile( "artifact.jar" );
        Gav gav =
            new Gav( groupId, artifactId, version, null, "jar", null, null, null, false, false, null, false, null );
        File downloaded;
        try
        {
            downloaded = downloadArtifactFromRepository( repositoryId, gav, "target/downloads/" + groupId );
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
    throws IOException
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
            //expected
        }
    }

    protected void checkArtifactOnGroup( String nexusGroupId, String groupId, String artifactId, String version )
        throws IOException
    {
        File artifact = getTestFile( "artifact.jar" );
        Gav gav =
            new Gav( groupId, artifactId, version, null, "jar", null, null, null, false, false, null, false, null );
        File downloaded;
        try
        {
            downloaded = downloadArtifactFromGroup( nexusGroupId, gav, "target/downloads/" + groupId );
        }
        catch ( IOException e )
        {
            Assert.fail( "Unable to download artifact " + artifactId + " got:\n" + e.getMessage() );
            throw e; // never happen
        }

        Assert.assertTrue( "Downloaded artifact was not right, checksum comparation fail " + artifactId,
                           FileTestingUtils.compareFileSHA1s( artifact, downloaded ) );
    }

    protected void checkIndex( String groupId, String artifactId, String version )
        throws Exception
    {
        List<NexusArtifact> artifacts = searchUtil.searchFor( groupId, artifactId, version );
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

        // the import is scheduled task now, so we need to wait for it to finish
        TaskScheduleUtil.waitForTasks( 40 );
    }

}
