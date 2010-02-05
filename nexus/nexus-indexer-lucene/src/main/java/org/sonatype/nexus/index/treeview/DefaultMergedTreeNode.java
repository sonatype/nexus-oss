/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.index.treeview;

/**
 * Enhances the DefaultTreeNode (which is built from index information) to add information that
 * Nexus has stored about the item.
 */
public class DefaultMergedTreeNode
    extends DefaultTreeNode
{
    /**
     * Flag that states whether the node is locally available.
     */
    private boolean locallyAvailable;

    /**
     * The timestamp the artifact was last modified..
     */
    private long artifactTimestamp;

    /**
     * The sha1 checksum of the artifact.
     */
    private String artifactSha1Checksum;

    /**
     * The md5 checksum of the artifact.
     */
    private String artifactMd5Checksum;

    /**
     * The user id that initiated Nexus to store this artifact.
     */
    private String initiatorUserId;

    /**
     * The ip address that initiated Nexus to store this artifact. 
     */
    private String initiatorIpAddress;

    /**
     * The reason this artifact is in Nexus (i.e. cached from proxy repository or deployed into hosted repository).
     */
    private String artifactOriginReason;

    /**
     * The remote url that this artifact was retrieved from.
     */
    private String artifactOriginUrl;

    /**
     * Constructor that takes an IndexTreeView implmentation and a TreeNodeFactory implementation;
     * 
     * @param tview
     * @param factory
     */
    public DefaultMergedTreeNode( IndexTreeView tview, TreeNodeFactory factory )
    {
        super( tview, factory );
    }

    /**
     * Get Flag that states whether the node is locally available.
     * 
     * @return boolean
     */
    public boolean isLocallyAvailable()
    {
        return locallyAvailable;
    }

    /**
     * Set flag that states whether the node is locally available.
     * 
     * @param boolean
     */
    public void setLocallyAvailable( boolean locallyAvailable )
    {
        this.locallyAvailable = locallyAvailable;
    }

    /**
     * Get the timestamp the artifact was last modified..
     * 
     * @return long
     */
    public long getArtifactTimestamp()
    {
        return artifactTimestamp;
    }

    /**
     * Set the timestamp the artifact was last modified..
     * 
     * @param long
     */
    public void setArtifactTimestamp( long artifactTimestamp )
    {
        this.artifactTimestamp = artifactTimestamp;
    }

    /**
     * Get the sha1 checksum of the artifact.
     * 
     * @return String
     */
    public String getArtifactSha1Checksum()
    {
        return artifactSha1Checksum;
    }

    /**
     * Set the sha1 checksum of the artifact.
     * 
     * @param String
     */
    public void setArtifactSha1Checksum( String artifactSha1Checksum )
    {
        this.artifactSha1Checksum = artifactSha1Checksum;
    }

    /**
     * Get the md5 checksum of the artifact.
     * 
     * @return String
     */
    public String getArtifactMd5Checksum()
    {
        return artifactMd5Checksum;
    }

    /**
     * Set the sha1 checksum of the artifact.
     * 
     * @param String
     */
    public void setArtifactMd5Checksum( String artifactMd5Checksum )
    {
        this.artifactMd5Checksum = artifactMd5Checksum;
    }

    /**
     * @return
     */
    public String getInitiatorUserId()
    {
        return initiatorUserId;
    }

    /**
     * @param initiatorUserId
     */
    public void setInitiatorUserId( String initiatorUserId )
    {
        this.initiatorUserId = initiatorUserId;
    }

    /**
     * @return
     */
    public String getInitiatorIpAddress()
    {
        return initiatorIpAddress;
    }

    /**
     * @param initiatorIpAddress
     */
    public void setInitiatorIpAddress( String initiatorIpAddress )
    {
        this.initiatorIpAddress = initiatorIpAddress;
    }

    /**
     * @return
     */
    public String getArtifactOriginReason()
    {
        return artifactOriginReason;
    }

    /**
     * @param artifactOriginReason
     */
    public void setArtifactOriginReason( String artifactOriginReason )
    {
        this.artifactOriginReason = artifactOriginReason;
    }

    /**
     * @return
     */
    public String getArtifactOriginUrl()
    {
        return artifactOriginUrl;
    }

    /**
     * @param artifactOriginUrl
     */
    public void setArtifactOriginUrl( String artifactOriginUrl )
    {
        this.artifactOriginUrl = artifactOriginUrl;
    }

}
