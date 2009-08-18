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
    private boolean localUrlChanged = false;
    private boolean remoteUrlChanged = false;
    private boolean downloadRemoteIndexEnabled = false;
    
    public RepositoryConfigurationUpdatedEvent( Repository repository )
    {
        super( repository );
    }
    
    public boolean isLocalUrlChanged()
    {
        return localUrlChanged;
    }
    
    public boolean isRemoteUrlChanged()
    {
        return remoteUrlChanged;
    }
    
    public boolean isDownloadRemoteIndexEnabled()
    {
        return downloadRemoteIndexEnabled;
    }
    
    public void setLocalUrlChanged( boolean localUrlChanged )
    {
        this.localUrlChanged = localUrlChanged;
    }
    
    public void setRemoteUrlChanged( boolean remoteUrlChanged )
    {
        this.remoteUrlChanged = remoteUrlChanged;
    }
    
    public void setDownloadRemoteIndexEnabled( boolean downloadRemoteIndexEnabled )
    {
        this.downloadRemoteIndexEnabled = downloadRemoteIndexEnabled;
    }
}
