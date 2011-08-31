package org.sonatype.nexus.bundle.launcher.util;

/**
 * Resolves artifacts from Maven repositories.
 */
public interface ArtifactResolver
{

    /**
     * Resolves an artifact using specified artifact coordinates.
     *
     * @param coordinates The artifact coordinates in the format
     *            {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not be {@code null}.
     * @return immutable resolved artifact, never {@code null}.
     */
    ResolvedArtifact resolveArtifact( String coordinates );

}
