package org.sonatype.nexus.plugin.migration.artifactory.util;

import org.sonatype.nexus.plugin.migration.artifactory.dto.FileLocationRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.FileLocationResource;
import org.sonatype.nexus.plugin.migration.artifactory.dto.GroupResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryResponseDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.UserResolutionDTO;
import org.sonatype.plexus.rest.xstream.AliasingListConverter;

import com.thoughtworks.xstream.XStream;

public class XStreamUtil
{

    public static void configureMigration( XStream xstream )
    {
        xstream.processAnnotations( MigrationSummaryDTO.class );
        xstream.processAnnotations( MigrationSummaryResponseDTO.class );
        xstream.processAnnotations( MigrationSummaryRequestDTO.class );
        xstream.processAnnotations( RepositoryResolutionDTO.class );
        xstream.processAnnotations( GroupResolutionDTO.class );
        xstream.processAnnotations( UserResolutionDTO.class );
        xstream.processAnnotations( FileLocationRequestDTO.class );
        xstream.processAnnotations( FileLocationResource.class );

        xstream.registerLocalConverter( MigrationSummaryDTO.class, "usersResolution",
                                        new AliasingListConverter( UserResolutionDTO.class, "userResolution" ) );

        xstream.registerLocalConverter( MigrationSummaryDTO.class, "repositoriesResolution",
                                        new AliasingListConverter( RepositoryResolutionDTO.class,
                                                                   "repositoryResolution" ) );

        xstream.registerLocalConverter( MigrationSummaryDTO.class, "groupsResolution",
                                        new AliasingListConverter( GroupResolutionDTO.class, "groupResolution" ) );
    }

}
