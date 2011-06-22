package org.sonatype.nexus.plugins.p2.repository.internal.capabilities;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.api.AbstractCapability;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryGenerator;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryGeneratorConfiguration;

public class P2RepositoryGeneratorCapability
    extends AbstractCapability
{

    public static final String ID = "p2RepositoryCapability";

    private final P2RepositoryGenerator service;

    private P2RepositoryGeneratorConfiguration configuration;

    public P2RepositoryGeneratorCapability( final String id, final P2RepositoryGenerator service )
    {
        super( id );
        this.service = service;
    }

    @Override
    public void create( final Map<String, String> properties )
    {
        configuration = new P2RepositoryGeneratorConfiguration( properties );
        service.addConfiguration( configuration );

        super.create( properties );
    }

    @Override
    public void load( final Map<String, String> properties )
    {
        configuration = new P2RepositoryGeneratorConfiguration( properties );
        service.addConfiguration( configuration );

        super.load( properties );
    }

    @Override
    public void update( final Map<String, String> properties )
    {
        final P2RepositoryGeneratorConfiguration newConfiguration = new P2RepositoryGeneratorConfiguration( properties );
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
