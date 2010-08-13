package org.sonatype.nexus.test;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.api.AbstractCapability;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.descriptor.RepositoryOrGroupCapabilityPropertyDescriptor;
import org.sonatype.nexus.proxy.NoSuchRepositoryException;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

public class TouchTestCapability
    extends AbstractCapability
    implements Capability
{

    public static final String ID = "TouchTest";

    private RepositoryRegistry repositoryRegistry;

    protected TouchTestCapability( String id, RepositoryRegistry repositoryRegistry )
    {
        super( id );
        this.repositoryRegistry = repositoryRegistry;
    }

    @Override
    public void create( Map<String, String> properties )
    {
        Repository repo = getRepository( properties );
        try
        {
            repo.storeItem( new ResourceStoreRequest( "/capability/test.txt" ), new ByteArrayInputStream(
                "capabilities test!".getBytes() ), null );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }

    }

    private Repository getRepository( final Map<String, String> properties )
    {
        String repositoryId = properties.get( RepositoryOrGroupCapabilityPropertyDescriptor.ID );
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
