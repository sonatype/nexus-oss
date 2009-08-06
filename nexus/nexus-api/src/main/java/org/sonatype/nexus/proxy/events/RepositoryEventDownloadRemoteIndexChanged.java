package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.repository.Repository;

public class RepositoryEventDownloadRemoteIndexChanged
    extends RepositoryEvent
{
    private final boolean oldValue;
    
    private final boolean newValue;
    
    public RepositoryEventDownloadRemoteIndexChanged( Repository repository, boolean oldValue, boolean newValue )
    {
        super( repository );
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    
    public boolean getOldValue()
    {
        return this.oldValue;
    }
    
    public boolean getNewValue()
    {
        return this.newValue;
    }
}
