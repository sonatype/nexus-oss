package org.sonatype.nexus.index.treeview;

public class DefaultMergedTreeNode
    extends DefaultTreeNode
{
    private boolean locallyAvailable;

    private long artifactTimestamp;

    private String artifactSha1Checksum;

    private String artifactMd5Checksum;

    private String initiatorUserId;

    private String initiatorIpAddress;

    private String artifactOriginReason;

    private String artifactOriginUrl;

    public DefaultMergedTreeNode( IndexTreeView tview, TreeNodeFactory factory )
    {
        super( tview, factory );
    }

    public boolean isLocallyAvailable()
    {
        return locallyAvailable;
    }

    public void setLocallyAvailable( boolean locallyAvailable )
    {
        this.locallyAvailable = locallyAvailable;
    }

    public long getArtifactTimestamp()
    {
        return artifactTimestamp;
    }

    public void setArtifactTimestamp( long artifactTimestamp )
    {
        this.artifactTimestamp = artifactTimestamp;
    }

    public String getArtifactSha1Checksum()
    {
        return artifactSha1Checksum;
    }

    public void setArtifactSha1Checksum( String artifactSha1Checksum )
    {
        this.artifactSha1Checksum = artifactSha1Checksum;
    }

    public String getArtifactMd5Checksum()
    {
        return artifactMd5Checksum;
    }

    public void setArtifactMd5Checksum( String artifactMd5Checksum )
    {
        this.artifactMd5Checksum = artifactMd5Checksum;
    }

    public String getInitiatorUserId()
    {
        return initiatorUserId;
    }

    public void setInitiatorUserId( String initiatorUserId )
    {
        this.initiatorUserId = initiatorUserId;
    }

    public String getInitiatorIpAddress()
    {
        return initiatorIpAddress;
    }

    public void setInitiatorIpAddress( String initiatorIpAddress )
    {
        this.initiatorIpAddress = initiatorIpAddress;
    }

    public String getArtifactOriginReason()
    {
        return artifactOriginReason;
    }

    public void setArtifactOriginReason( String artifactOriginReason )
    {
        this.artifactOriginReason = artifactOriginReason;
    }

    public String getArtifactOriginUrl()
    {
        return artifactOriginUrl;
    }

    public void setArtifactOriginUrl( String artifactOriginUrl )
    {
        this.artifactOriginUrl = artifactOriginUrl;
    }

}
