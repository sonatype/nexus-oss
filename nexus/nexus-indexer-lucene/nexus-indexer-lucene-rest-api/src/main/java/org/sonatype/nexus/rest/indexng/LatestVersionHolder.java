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

        maintainLatestVersions( ai );
    }

    @SuppressWarnings( "unchecked" )
    public boolean maintainLatestVersions( final ArtifactInfo ai )
    {
        @SuppressWarnings( "deprecation" )
        ArtifactVersion version = ai.getArtifactVersion();

        boolean versionChanged = false;

        if ( VersionUtils.isSnapshot( ai.version ) )
        {
            if ( this.latestSnapshot == null )
            {
                this.latestSnapshot = version;

                this.latestSnapshotRepositoryId = ai.repository;

                versionChanged = true;
            }
            else if ( this.latestSnapshot.compareTo( version ) < 0 )
            {
                this.latestSnapshot = version;

                this.latestSnapshotRepositoryId = ai.repository;

                versionChanged = true;
            }
        }
        else
        {
            if ( this.latestRelease == null )
            {
                this.latestRelease = version;

                this.latestReleaseRepositoryId = ai.repository;

                versionChanged = true;
            }
            else if ( this.latestRelease.compareTo( version ) < 0 )
            {
                this.latestRelease = version;

                this.latestReleaseRepositoryId = ai.repository;

                versionChanged = true;
            }
        }

        return versionChanged;
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
