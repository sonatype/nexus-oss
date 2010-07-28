package org.sonatype.nexus.rest.indexng;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.sonatype.nexus.artifact.VersionUtils;
import org.sonatype.nexus.index.ArtifactInfo;

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

    private ArtifactVersion latestSnapshot;

    private String latestSnapshotRepositoryId;

    private ArtifactVersion latestRelease;

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
    @SuppressWarnings( "unchecked" )
    public VersionChange maintainLatestVersions( final ArtifactInfo ai )
    {
        @SuppressWarnings( "deprecation" )
        ArtifactVersion version = ai.getArtifactVersion();

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

    public ArtifactVersion getLatestSnapshot()
    {
        return latestSnapshot;
    }

    public String getLatestSnapshotRepositoryId()
    {
        return latestSnapshotRepositoryId;
    }

    public ArtifactVersion getLatestRelease()
    {
        return latestRelease;
    }

    public String getLatestReleaseRepositoryId()
    {
        return latestReleaseRepositoryId;
    }
}
