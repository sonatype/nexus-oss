package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.proxy.repository.Repository;

public interface MavenRepository
    extends ArtifactStore, Repository
{
    GavCalculator getGavCalculator();

    ChecksumPolicy getChecksumPolicy();

    void setChecksumPolicy( ChecksumPolicy checksumPolicy );

    RepositoryPolicy getRepositoryPolicy();

    void setRepositoryPolicy( RepositoryPolicy repositoryPolicy );

    int getReleaseMaxAge();

    void setReleaseMaxAge( int releaseMaxAge );

    int getSnapshotMaxAge();

    void setSnapshotMaxAge( int snapshotMaxAge );

    int getMetadataMaxAge();

    void setMetadataMaxAge( int metadataMaxAge );

    boolean isCleanseRepositoryMetadata();

    void setCleanseRepositoryMetadata( boolean cleanseRepositoryMetadata );

    boolean isFixRepositoryChecksums();

    void setFixRepositoryChecksums( boolean fixRepositoryChecksums );
}
