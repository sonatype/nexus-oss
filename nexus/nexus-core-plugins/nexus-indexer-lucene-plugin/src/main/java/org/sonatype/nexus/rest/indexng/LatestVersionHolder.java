/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
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
