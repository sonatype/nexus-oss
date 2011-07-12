package org.sonatype.nexus.plugin.migration.artifactory;

import java.io.File;

import org.junit.Test;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.plugin.migration.artifactory.dto.FileLocationRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.FileLocationResource;
import org.sonatype.nexus.plugin.migration.artifactory.dto.GroupResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryResponseDTO;
import org.sonatype.plexus.rest.resource.PlexusResource;

public class ArtifactoryFileLocationPRTest
    extends AbstractNexusTestCase
{

    @Test
    public void testPost()
        throws Exception
    {
        System.setProperty( "plexus.log4j-prop-file",
                            new File( getBasedir(), "target/test-classes/log4j.properties" ).getAbsolutePath() );

        PlexusResource resource = this.lookup( PlexusResource.class, "artifactoryFileLocation" );

        FileLocationRequestDTO dto = new FileLocationRequestDTO();
        FileLocationResource fileLocationResource = new FileLocationResource();
        dto.setData( fileLocationResource );

        File backupFile = new File( "./target/test-classes/backup-files/artifactory130.zip" ).getCanonicalFile();
        System.out.println( "backupFile.getAbsolutePath(): " + backupFile.getAbsolutePath() );

        fileLocationResource.setFileLocation( backupFile.getAbsolutePath() );

        MigrationSummaryResponseDTO result = (MigrationSummaryResponseDTO) resource.post( null, null, null, dto );
        MigrationSummaryDTO resultDto = result.getData();

        assertEquals( backupFile, new File( resultDto.getBackupLocation() ) );

        // Nexus 1832
        GroupResolutionDTO repo = resultDto.getGroupResolution( "repo" );
        assertNotNull( repo );
        assertEquals( "repo", repo.getGroupId() );
    }

}
