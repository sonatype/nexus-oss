package org.sonatype.nexus.plugin.migration.artifactory.dto;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "migrationSummaryRequest" )
public class MigrationSummaryRequestDTO
{

    private MigrationSummaryDTO data;

    public MigrationSummaryDTO getData()
    {
        return data;
    }

    public void setData( MigrationSummaryDTO data )
    {
        this.data = data;
    }

}
