package org.sonatype.nexus.plugins;

public class PluginCoordinates
{
    private String groupId;

    private String artifactId;

    private String version;

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }
    
    public String toString()
    {
        return getGroupId() + ":" + getArtifactId() + ":" + getVersion();
    }
}
