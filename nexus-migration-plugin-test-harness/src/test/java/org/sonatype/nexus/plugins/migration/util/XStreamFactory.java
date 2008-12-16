package org.sonatype.nexus.plugins.migration.util;

import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryRequestDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryResponseDTO;
import org.sonatype.nexus.plugin.migration.artifactory.dto.RepositoryResolutionDTO;

import com.thoughtworks.xstream.XStream;

public class XStreamFactory
{

    public static XStream getXmlXStream()
    {
        XStream xs = org.sonatype.nexus.test.utils.XStreamFactory.getXmlXStream();

        xs.processAnnotations( MigrationSummaryDTO.class );
        xs.processAnnotations( MigrationSummaryResponseDTO.class );
        xs.processAnnotations( MigrationSummaryRequestDTO.class );
        xs.processAnnotations( RepositoryResolutionDTO.class );

        return xs;
    }

    public static XStream getJsonXStream()
    {
        XStream xs = org.sonatype.nexus.test.utils.XStreamFactory.getJsonXStream();

        xs.processAnnotations( MigrationSummaryDTO.class );
        xs.processAnnotations( MigrationSummaryResponseDTO.class );
        xs.processAnnotations( MigrationSummaryRequestDTO.class );
        xs.processAnnotations( RepositoryResolutionDTO.class );

        return xs;
    }

}
