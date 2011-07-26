package org.sonatype.nexus.bundle.launcher.util;

import java.io.File;

/**
 * Resolves artifacts from Maven repositories.
 */
public interface ArtifactResolver
{

    /**
     * Resolves the specified artifact.
     *
     * @param coordinates The artifact coordinates in the format
     *            {@code <groupId>:<artifactId>[:<extension>[:<classifier>]]:<version>}, must not be {@code null}.
     * @return The path to the resolved artifact, never {@code null}.
     */
    File resolve( String coordinates );

}
