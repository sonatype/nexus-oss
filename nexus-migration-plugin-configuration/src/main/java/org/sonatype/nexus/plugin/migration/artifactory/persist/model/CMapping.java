package org.sonatype.nexus.plugin.migration.artifactory.persist.model;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias( "mapping" )
public class CMapping
{

    private String artifactoryRepositoryId;

    private String nexusGroupId;

    private String nexusRepositoryId;

    private String releasesRepositoryId;

    private String snapshotsRepositoryId;

    public CMapping()
    {
        super();
    }

    public CMapping( String artifactoryRepositoryId, String nexusRepositoryId )
    {
        this();
        this.artifactoryRepositoryId = artifactoryRepositoryId;
        this.nexusRepositoryId = nexusRepositoryId;
    }

    public CMapping( String artifactoryRepositoryId, String nexusGroupId, String releasesRepositoryId,
                     String snapshotsRepositoryId )
    {
        this();
        this.artifactoryRepositoryId = artifactoryRepositoryId;
        this.nexusGroupId = nexusGroupId;
        this.releasesRepositoryId = releasesRepositoryId;
        this.snapshotsRepositoryId = snapshotsRepositoryId;
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

    public String getReleasesRepositoryId()
    {
        return releasesRepositoryId;
    }

    public void setReleasesRepositoryId( String releasesRepositoryId )
    {
        this.releasesRepositoryId = releasesRepositoryId;
    }

    public String getSnapshotsRepositoryId()
    {
        return snapshotsRepositoryId;
    }

    public void setSnapshotsRepositoryId( String snapshotsRepositoryId )
    {
        this.snapshotsRepositoryId = snapshotsRepositoryId;
    }

}
