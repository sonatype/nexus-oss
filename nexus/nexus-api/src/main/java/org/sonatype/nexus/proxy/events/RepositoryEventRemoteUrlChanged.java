package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.repository.Repository;

public class RepositoryEventRemoteUrlChanged
    extends RepositoryEvent
{
    private final String oldRemoteUrl;

    private final String newRemoteUrl;

    public RepositoryEventRemoteUrlChanged( Repository repository, String oldRemoteUrl, String newRemoteUrl )
    {
        super( repository );

        this.oldRemoteUrl = ( oldRemoteUrl == null ? "" : oldRemoteUrl );

        this.newRemoteUrl = ( newRemoteUrl == null ? "" : newRemoteUrl );
    }

    public String getOldRemoteUrl()
    {
        return oldRemoteUrl;
    }

    public String getNewRemoteUrl()
    {
        return newRemoteUrl;
    }

}
