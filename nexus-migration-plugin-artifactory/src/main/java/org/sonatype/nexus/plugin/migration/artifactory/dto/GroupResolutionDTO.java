package org.sonatype.nexus.plugin.migration.artifactory.dto;

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
