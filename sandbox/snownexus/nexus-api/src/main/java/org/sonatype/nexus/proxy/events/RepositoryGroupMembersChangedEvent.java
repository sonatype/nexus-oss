package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.repository.GroupRepository;

/**
 * Fired when a group repository members changed and is applied (not rollbacked).
 * 
 * @author cstamas
 */
public class RepositoryGroupMembersChangedEvent
    extends RepositoryEvent
{
    public RepositoryGroupMembersChangedEvent( GroupRepository repository )
    {
        super( repository );
    }

    public GroupRepository getGroupRepository()
    {
        return (GroupRepository) getEventSender();
    }
}
