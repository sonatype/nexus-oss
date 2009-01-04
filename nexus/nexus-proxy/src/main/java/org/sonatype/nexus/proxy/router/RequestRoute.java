package org.sonatype.nexus.proxy.router;

import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.repository.Repository;

public class RequestRoute
{
    private Repository targetedRepository;

    private String repositoryPath;

    private String strippedPrefix;

    private String originalRequestPath;

    private ResourceStoreRequest resourceStoreRequest;

    public boolean isRepositoryHit()
    {
        return targetedRepository != null;
    }

    public Repository getTargetedRepository()
    {
        return targetedRepository;
    }

    public void setTargetedRepository( Repository targetedRepository )
    {
        this.targetedRepository = targetedRepository;
    }

    public String getRepositoryPath()
    {
        return repositoryPath;
    }

    public void setRepositoryPath( String repositoryPath )
    {
        this.repositoryPath = repositoryPath;
    }

    public String getStrippedPrefix()
    {
        return strippedPrefix;
    }

    public void setStrippedPrefix( String strippedPrefix )
    {
        this.strippedPrefix = strippedPrefix;
    }

    public String getOriginalRequestPath()
    {
        return originalRequestPath;
    }

    public void setOriginalRequestPath( String originalRequestPath )
    {
        this.originalRequestPath = originalRequestPath;
    }

    public ResourceStoreRequest getResourceStoreRequest()
    {
        return resourceStoreRequest;
    }

    public void setResourceStoreRequest( ResourceStoreRequest resourceStoreRequest )
    {
        this.resourceStoreRequest = resourceStoreRequest;
    }
}
