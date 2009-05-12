package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Fired when a repository configuration changed and is applied (not rollbacked).
 * 
 * @author cstamas
 */
public class RepositoryConfigurationUpdatedEvent
    extends RepositoryEvent
{
    public RepositoryConfigurationUpdatedEvent( Repository repository )
    {
        super( repository );
    }
}
