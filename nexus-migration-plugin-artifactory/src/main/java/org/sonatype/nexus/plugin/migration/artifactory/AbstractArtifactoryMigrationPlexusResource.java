package org.sonatype.nexus.plugin.migration.artifactory;

import org.sonatype.nexus.plugin.migration.artifactory.dto.GroupResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryResponseDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.UserResolutionDTO;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;

import com.thoughtworks.xstream.XStream;

public abstract class AbstractArtifactoryMigrationPlexusResource
    extends AbstractNexusPlexusResource
{

    public AbstractArtifactoryMigrationPlexusResource()
    {
        super();
    }

    @Override
    public void configureXStream( XStream xstream )
    {
        super.configureXStream( xstream );

        xstream.processAnnotations( MigrationSummaryDTO.class );
        xstream.processAnnotations( MigrationSummaryResponseDTO.class );
        xstream.processAnnotations( MigrationSummaryRequestDTO.class );
        xstream.processAnnotations( RepositoryResolutionDTO.class );
        xstream.processAnnotations( GroupResolutionDTO.class );
        xstream.processAnnotations( UserResolutionDTO.class );
    }

}