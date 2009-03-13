package org.sonatype.nexus.proxy.repository;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.item.RepositoryItemUid;
import org.sonatype.nexus.proxy.item.StorageItem;

public class RepositoryRequest
{
    private final Repository repository;

    private final ResourceStoreRequest request;

    public RepositoryRequest( StorageItem item, ResourceStoreRequest request )
    {
        this( item.getRepositoryItemUid(), request );
    }

    public RepositoryRequest( RepositoryItemUid uid, ResourceStoreRequest request )
    {
        this( uid.getRepository(), request );
    }

    public RepositoryRequest( Repository repository, ResourceStoreRequest request )
    {
        this.repository = repository;

        this.request = request;
    }

    public Repository getRepository()
    {
        return repository;
    }

    public ResourceStoreRequest getResourceStoreRequest()
    {
        return request;
    }

    public String toString()
    {
        return getRepository().getId() + ":" + getResourceStoreRequest().getRequestPath();
    }
}
