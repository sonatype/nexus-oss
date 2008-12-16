package org.sonatype.nexus.plugin.migration.artifactory.dto;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("migrationSummaryDTO")
public class MigrationSummaryDTO
{

    private String backupLocation;

    //TODO need security related stuff

    private List<RepositoryResolutionDTO> repositoriesResolution;

    public List<RepositoryResolutionDTO> getRepositoriesResolution()
    {
        return repositoriesResolution;
    }

    public void setRepositoriesResolution( List<RepositoryResolutionDTO> repositoriesResolution )
    {
        this.repositoriesResolution = repositoriesResolution;
    }

    public String getBackupLocation()
    {
        return backupLocation;
    }

    public void setBackupLocation( String backupLocation )
    {
        this.backupLocation = backupLocation;
    }

}
