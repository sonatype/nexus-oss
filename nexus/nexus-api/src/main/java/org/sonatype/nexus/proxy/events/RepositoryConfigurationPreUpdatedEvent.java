package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Fired before a repository configuration changed and is applied (still unsaved).
 *
 * @author velo
 */
public class RepositoryConfigurationPreUpdatedEvent
    extends RepositoryEvent
{
    public RepositoryConfigurationPreUpdatedEvent( Repository repository )
    {
        super( repository );
    }
}
