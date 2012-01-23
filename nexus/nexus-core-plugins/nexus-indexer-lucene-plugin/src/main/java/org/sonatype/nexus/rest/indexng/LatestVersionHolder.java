/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.indexng;

import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.artifact.VersionUtils;
import org.sonatype.aether.version.Version;

/**
 * A holder for the latest version (both release and snapshot).
 * 
 * @author cstamas
 */
public class LatestVersionHolder
{
    public enum VersionChange
    {
        SMALLER, EQUALS, GREATER;
    };

    private final String groupId;

    private final String artifactId;

    private Version latestSnapshot;

    private String latestSnapshotRepositoryId;

    private Version latestRelease;

    private String latestReleaseRepositoryId;

    public LatestVersionHolder( final ArtifactInfo ai )
    {
        this.groupId = ai.groupId;

        this.artifactId = ai.artifactId;
    }

    /**
     * Maintains the LatestVersionHolder and returns VersionChange how passed in ArtifactInfo relates to current state
     * of this object.
     * 
     * @param ai
     * @return relation of passed in ai paramter to current state
     */
    public VersionChange maintainLatestVersions( final ArtifactInfo ai )
    {
        Version version = ai.getArtifactVersion();

        VersionChange versionChange = VersionChange.EQUALS;

        if ( VersionUtils.isSnapshot( ai.version ) )
        {
            if ( this.latestSnapshot == null )
            {
                this.latestSnapshot = version;

                this.latestSnapshotRepositoryId = ai.repository;

                versionChange = VersionChange.GREATER;
            }
            else
            {
                int cmp = latestSnapshot.compareTo( version );

                if ( cmp < 0 )
                {
                    this.latestSnapshot = version;

                    this.latestSnapshotRepositoryId = ai.repository;

                    versionChange = VersionChange.GREATER;
                }
                else if ( cmp == 0 )
                {
                    versionChange = VersionChange.EQUALS;
                }
                else
                {
                    versionChange = VersionChange.SMALLER;
                }
            }
        }
        else
        {
            if ( this.latestRelease == null )
            {
                this.latestRelease = version;

                this.latestReleaseRepositoryId = ai.repository;

                versionChange = VersionChange.GREATER;
            }
            else
            {
                int cmp = latestRelease.compareTo( version );

                if ( cmp < 0 )
                {
                    this.latestRelease = version;

                    this.latestReleaseRepositoryId = ai.repository;

                    versionChange = VersionChange.GREATER;
                }
                else if ( cmp == 0 )
                {
                    versionChange = VersionChange.EQUALS;
                }
                else
                {
                    versionChange = VersionChange.SMALLER;
                }
            }
        }

        return versionChange;
    }

    // ==

    public String getGroupId()
    {
        return groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public Version getLatestSnapshot()
    {
        return latestSnapshot;
    }

    public String getLatestSnapshotRepositoryId()
    {
        return latestSnapshotRepositoryId;
    }

    public Version getLatestRelease()
    {
        return latestRelease;
    }

    public String getLatestReleaseRepositoryId()
    {
        return latestReleaseRepositoryId;
    }
}
