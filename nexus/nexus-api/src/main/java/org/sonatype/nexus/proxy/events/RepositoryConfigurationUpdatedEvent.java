package org.sonatype.nexus.proxy.events;

import java.util.Map;

import org.sonatype.nexus.proxy.repository.Repository;

/**
 * Fired when a repository configuration changed and is applied (not rollbacked).
 * 
 * @author cstamas
 */
public class RepositoryConfigurationUpdatedEvent
    extends RepositoryEvent
{
    Map<String, Object> changes;
    public RepositoryConfigurationUpdatedEvent( Repository repository, Map<String,Object> changes )
    {
        super( repository );
        
        this.changes = changes;
    }
    
    public Map<String, Object> getChanges()
    {
        return changes;
    }
}
