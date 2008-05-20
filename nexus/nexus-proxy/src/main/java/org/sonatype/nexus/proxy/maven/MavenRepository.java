package org.sonatype.nexus.proxy.maven;

import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.proxy.repository.Repository;

public interface MavenRepository
    extends ArtifactStore, Repository
{
    RepositoryPolicy getRepositoryPolicy();

    GavCalculator getGavCalculator();
}
