package org.sonatype.nexus.test;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.api.AbstractCapability;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
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
        doIt( properties );
    }

    @Override
    public void update( Map<String, String> properties )
    {
        doIt( properties );
    }

    private void doIt( final Map<String, String> properties )
    {
        final Repository repo = getRepository( properties );

        try
        {
            repo.storeItem(
                new ResourceStoreRequest( "/capability/test.txt" ),
                new ByteArrayInputStream(
                    ( "capabilities test!\n" + properties.get( TouchTestCapabilityDescriptor.FIELD_REPO_OR_GROUP_ID ) ).getBytes() ),
                null );
        }
        catch ( Exception e )
        {
            throw new RuntimeException( e.getMessage(), e );
        }

    }

    private Repository getRepository( final Map<String, String> properties )
    {
        String repositoryId = properties.get( TouchTestCapabilityDescriptor.FIELD_REPO_OR_GROUP_ID );
        final Repository repo;
        try
        {
            repositoryId = repositoryId.replaceFirst( "repo_", "" );
            repositoryId = repositoryId.replaceFirst( "group_", "" );
            repo = repositoryRegistry.getRepository( repositoryId );
        }
        catch ( final NoSuchRepositoryException e )
        {
            throw new RuntimeException( String.format( "Cannot find repository %s", repositoryId ) );
        }
        return repo;
    }

}
