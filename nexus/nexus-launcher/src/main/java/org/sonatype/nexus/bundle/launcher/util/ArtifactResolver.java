package org.sonatype.nexus.bundle.launcher.util;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Resolves artifacts from Maven repositories.
 */
public interface ArtifactResolver
{

    /**
     * Resolves an artifact using specified artifact coordinates.
     *
     * @param coordinate The artifact coordinates in the format
     *            {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not be {@code null}.
     * @return immutable resolved artifact, never {@code null}.
     */
    ResolvedArtifact resolveArtifact( String coordinate );

    /**
     * Resolves artifacts using the set of specified artifact coordinates.
     *
     * @param coordinates A Set of artifact coordinates in the format
     *            {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not be {@code null}.
     * @return immutable set of resolved artifacts, never {@code null}.
     */
    List<ResolvedArtifact> resolveArtifacts(Collection<String> coordinates);

}
