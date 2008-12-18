package org.sonatype.nexus.plugin.migration.artifactory.dto;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("migrationSummaryDTO")
public class MigrationSummaryDTO
{

    private String backupLocation;

    private List<UserResolutionDTO> userResolution;
    
    private boolean resolvePermission;

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

    public List<UserResolutionDTO> getUserResolution()
    {
        return userResolution;
    }

    public void setUserResolution( List<UserResolutionDTO> userResolution )
    {
        this.userResolution = userResolution;
    }

    public boolean isResolvePermission()
    {
        return resolvePermission;
    }

    public void setResolvePermission( boolean resolvePermission )
    {
        this.resolvePermission = resolvePermission;
    }

}
