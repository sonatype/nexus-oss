/**
 * Copyright © 2008 Sonatype, Inc. All rights reserved.
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
package org.sonatype.nexus.plugin.migration.artifactory.dto;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "migrationSummaryDTO" )
public class MigrationSummaryDTO
{

    private String backupLocation;

    private List<UserResolutionDTO> userResolution;

    private boolean resolvePermission;

    private List<RepositoryResolutionDTO> repositoriesResolution;

    private List<GroupResolutionDTO> groupsResolution;

    public List<RepositoryResolutionDTO> getRepositoriesResolution()
    {
        if ( repositoriesResolution == null )
        {
            repositoriesResolution = new ArrayList<RepositoryResolutionDTO>();
        }
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
        if ( userResolution == null )
        {
            userResolution = new ArrayList<UserResolutionDTO>();
        }
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

    public void setGroupsResolution( List<GroupResolutionDTO> groupsResolution )
    {
        this.groupsResolution = groupsResolution;
    }

    public List<GroupResolutionDTO> getGroupsResolution()
    {
        if ( groupsResolution == null )
        {
            groupsResolution = new ArrayList<GroupResolutionDTO>();
        }
        return groupsResolution;
    }

    public RepositoryResolutionDTO getRepositoryResolution( String repoId )
    {
        if ( repoId == null )
        {
            return null;
        }

        for ( RepositoryResolutionDTO resolution : getRepositoriesResolution() )
        {
            if ( repoId.equals( resolution.getRepositoryId() ) )
            {
                return resolution;
            }
        }
        return null;
    }

    public GroupResolutionDTO getGroupResolution( String groupId )
    {
        if ( groupId == null )
        {
            return null;
        }

        for ( GroupResolutionDTO resolution : getGroupsResolution() )
        {
            if ( groupId.equals( resolution.getGroupId() ) )
            {
                return resolution;
            }
        }
        return null;
    }

}
