package org.sonatype.nexus.plugin.migration.artifactory.persist.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "mapping" )
public class CMapping
{

    private String artifactoryRepositoryId;

    private String nexusGroupId;

    private String nexusRepositoryId;

    public CMapping()
    {
        super();
    }

    public CMapping( String artifactoryRepositoryId, String nexusGroupId, String nexusRepositoryId )
    {
        this();
        this.artifactoryRepositoryId = artifactoryRepositoryId;
        this.nexusGroupId = nexusGroupId;
        this.nexusRepositoryId = nexusRepositoryId;
    }

    public String getArtifactoryRepositoryId()
    {
        return artifactoryRepositoryId;
    }

    public String getNexusGroupId()
    {
        return nexusGroupId;
    }

    public String getNexusRepositoryId()
    {
        return nexusRepositoryId;
    }

    public void setArtifactoryRepositoryId( String artifactoryRepositoryId )
    {
        this.artifactoryRepositoryId = artifactoryRepositoryId;
    }

    public void setNexusGroupId( String nexusGroupId )
    {
        this.nexusGroupId = nexusGroupId;
    }

    public void setNexusRepositoryId( String nexusRepositoryId )
    {
        this.nexusRepositoryId = nexusRepositoryId;
    }

}
