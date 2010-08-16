package org.sonatype.nexus.plugins.capabilities.api;

import java.util.Map;

import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

public abstract class AbstractRepositoryCapability
    extends AbstractCapability
{
    private final RepositoryRegistry repositoryRegistry;
    
    public AbstractRepositoryCapability( final String id, final RepositoryRegistry repositoryRegistry )
    {
        super( id );
        this.repositoryRegistry = repositoryRegistry;
    }
    
    protected Repository getRepository( String id, final Map<String, String> properties )
    {
        String repositoryId = properties.get( id );
        try
        {
            repositoryId = repositoryId.replaceFirst( "repo_", "" );
            repositoryId = repositoryId.replaceFirst( "group_", "" );
            final Repository found = repositoryRegistry.getRepository( repositoryId );
            return found;
        }
        catch ( final NoSuchRepositoryException e )
        {
            throw new RuntimeException( String.format( "Cannot find repository %s", repositoryId ) );
        }
    }
}
