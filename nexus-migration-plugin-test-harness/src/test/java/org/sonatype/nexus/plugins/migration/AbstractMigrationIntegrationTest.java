package org.sonatype.nexus.plugins.migration;

import hidden.org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.sonatype.nexus.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.UserResolutionDTO;
import org.sonatype.nexus.plugins.migration.util.ImportMessageUtil;
import org.sonatype.nexus.rest.model.NexusArtifact;
import org.sonatype.nexus.rest.model.RepositoryGroupListResource;
import org.sonatype.nexus.rest.model.RepositoryListResource;
import org.sonatype.nexus.test.utils.FileTestingUtils;
import org.sonatype.nexus.test.utils.GroupMessageUtil;
import org.sonatype.nexus.test.utils.RepositoryMessageUtil;
import org.sonatype.nexus.test.utils.SearchMessageUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

public class AbstractMigrationIntegrationTest
    extends AbstractNexusIntegrationTest
{
    protected static final String DEFAULT_EMAIL = "juven@mars.com";
    
    protected RepositoryMessageUtil repositoryUtil;

    protected GroupMessageUtil groupUtil;

    protected SearchMessageUtil searchUtil;

    public AbstractMigrationIntegrationTest()
    {
        this.repositoryUtil = new RepositoryMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
        this.groupUtil = new GroupMessageUtil( this.getXMLXStream(), MediaType.APPLICATION_XML );
        this.searchUtil = new SearchMessageUtil();
    }

    @BeforeClass
    public static void clean()
        throws IOException
    {
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
            + "\n" + artifacts, 1, artifacts.size() );
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
        fillDefaultEmailIfNotExist( migrationSummary.getUserResolution() );
        return migrationSummary;
    }
    
    protected void commitMigration( MigrationSummaryDTO migrationSummary )
        throws IOException
    {
        Status status = ImportMessageUtil.commitImport( migrationSummary ).getStatus();
        Assert.assertTrue( "Unable to commit import " + status, status.isSuccess() );
    }

    protected void fillDefaultEmailIfNotExist( List<UserResolutionDTO> resolutions )
    {
        for ( UserResolutionDTO resolution : resolutions )
        {
            if ( StringUtils.isEmpty( resolution.getEmail() ) )
            {
                resolution.setEmail( DEFAULT_EMAIL );
            }
        }
    }
}
