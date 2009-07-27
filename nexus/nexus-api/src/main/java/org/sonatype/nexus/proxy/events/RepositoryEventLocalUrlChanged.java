package org.sonatype.nexus.proxy.events;

import org.sonatype.nexus.proxy.repository.Repository;

public class RepositoryEventLocalUrlChanged
    extends RepositoryEvent
{
    private final String oldLocalUrl;

    private final String newLocalUrl;

    public RepositoryEventLocalUrlChanged( Repository repository, String oldLocalUrl, String newLocalUrl )
    {
        super( repository );

        this.oldLocalUrl = ( oldLocalUrl == null ? "" : oldLocalUrl );

        this.newLocalUrl = ( newLocalUrl == null ? "" : newLocalUrl );
    }

    public String getOldLocalUrl()
    {
        return oldLocalUrl;
    }

    public String getNewLocalUrl()
    {
        return newLocalUrl;
    }

}
