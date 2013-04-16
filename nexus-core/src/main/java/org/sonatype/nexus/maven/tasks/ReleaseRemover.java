package org.sonatype.nexus.maven.tasks;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;

/**
 * @since 20.5
 */
public interface ReleaseRemover
{

    ReleaseRemovalResult removeReleases( ReleaseRemovalRequest request )
        throws NoSuchRepositoryException;
}
