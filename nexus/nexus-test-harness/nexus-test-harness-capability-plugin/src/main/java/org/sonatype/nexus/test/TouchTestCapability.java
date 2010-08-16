package org.sonatype.nexus.test;

import java.io.ByteArrayInputStream;
import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.api.AbstractRepositoryCapability;
import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.proxy.ResourceStoreRequest;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;
import org.sonatype.nexus.proxy.repository.Repository;

public class TouchTestCapability
    extends AbstractRepositoryCapability
    implements Capability
{

    public static final String ID = "TouchTest";

    protected TouchTestCapability( String id, RepositoryRegistry repositoryRegistry )
    {
        super( id, repositoryRegistry );
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
        final Repository repo = getRepository( TouchTestCapabilityDescriptor.FIELD_REPO_OR_GROUP_ID, properties );

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
}
