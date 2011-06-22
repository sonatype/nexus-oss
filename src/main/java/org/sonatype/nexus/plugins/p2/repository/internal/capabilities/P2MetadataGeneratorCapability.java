package org.sonatype.nexus.plugins.p2.repository.internal.capabilities;

import java.util.Map;

import org.sonatype.nexus.plugins.capabilities.api.AbstractCapability;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGenerator;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGeneratorConfiguration;

public class P2MetadataGeneratorCapability
    extends AbstractCapability
{

    public static final String ID = "p2MetadataCapability";

    private final P2MetadataGenerator service;

    private P2MetadataGeneratorConfiguration configuration;

    public P2MetadataGeneratorCapability( final String id, final P2MetadataGenerator service )
    {
        super( id );
        this.service = service;
    }

    @Override
    public void create( final Map<String, String> properties )
    {
        configuration = new P2MetadataGeneratorConfiguration( properties );
        service.addConfiguration( configuration );

        super.create( properties );
    }

    @Override
    public void load( final Map<String, String> properties )
    {
        configuration = new P2MetadataGeneratorConfiguration( properties );
        service.addConfiguration( configuration );

        super.load( properties );
    }

    @Override
    public void update( final Map<String, String> properties )
    {
        final P2MetadataGeneratorConfiguration newConfiguration = new P2MetadataGeneratorConfiguration( properties );
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
