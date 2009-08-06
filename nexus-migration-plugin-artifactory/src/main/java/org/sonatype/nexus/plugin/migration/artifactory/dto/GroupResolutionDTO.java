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
package org.sonatype.nexus.plugin.migration.artifactory.dto;

import org.codehaus.plexus.util.StringUtils;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "groupResolution" )
public class GroupResolutionDTO
{
    private String groupId;

    private boolean isMixed = false;

    private String repositoryTypeResolution = ERepositoryTypeResolution.MAVEN_2_ONLY.name();

    public GroupResolutionDTO()
    {
        super();
    }

    public GroupResolutionDTO( String groupId, boolean isMixed )
    {
        super();
        this.groupId = groupId;
        this.isMixed = isMixed;
    }

    public String getGroupId()
    {
        return groupId;
    }

    public ERepositoryTypeResolution getRepositoryTypeResolution()
    {
        if ( StringUtils.isEmpty( repositoryTypeResolution ) )
        {
            return null;
        }
        return ERepositoryTypeResolution.valueOf( repositoryTypeResolution );
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public void setRepositoryTypeResolution( ERepositoryTypeResolution repositoryTypeResolution )
    {
        this.repositoryTypeResolution = repositoryTypeResolution.name();
    }

    public boolean isMixed()
    {
        return isMixed;
    }

    public void setMixed( boolean isMixed )
    {
        this.isMixed = isMixed;
    }

}
