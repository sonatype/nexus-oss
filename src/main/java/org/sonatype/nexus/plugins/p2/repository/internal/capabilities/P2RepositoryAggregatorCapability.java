package org.sonatype.nexus.plugins.p2.repository.internal.capabilities;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.api.AbstractCapability;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregator;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregatorConfiguration;

public class P2RepositoryAggregatorCapability
    extends AbstractCapability
{

    public static final String ID = "p2RepositoryAggregatorCapability";

    private final P2RepositoryAggregator service;

    private P2RepositoryAggregatorConfiguration configuration;

    public P2RepositoryAggregatorCapability( final String id, final P2RepositoryAggregator service )
    {
        super( id );
        this.service = service;
    }

    @Override
    public void create( final Map<String, String> properties )
    {
        configuration = new P2RepositoryAggregatorConfiguration( properties );
        service.addConfiguration( configuration );

        super.create( properties );
    }

    @Override
    public void load( final Map<String, String> properties )
    {
        configuration = new P2RepositoryAggregatorConfiguration( properties );
        service.addConfiguration( configuration );

        super.load( properties );
    }

    @Override
    public void update( final Map<String, String> properties )
    {
        final P2RepositoryAggregatorConfiguration newConfiguration = new P2RepositoryAggregatorConfiguration( properties );
        if ( !configuration.equals( newConfiguration ) )
        {
            remove();
            create( properties );
        }

        super.update( properties );
    }

    @Override
    public void remove()
    {
        service.removeConfiguration( configuration );

        super.remove();
    }

}
