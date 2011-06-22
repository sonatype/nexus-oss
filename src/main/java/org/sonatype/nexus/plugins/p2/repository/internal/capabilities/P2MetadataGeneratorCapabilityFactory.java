package org.sonatype.nexus.plugins.p2.repository.internal.capabilities;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGenerator;

@Named( P2MetadataGeneratorCapability.ID )
@Singleton
public class P2MetadataGeneratorCapabilityFactory
    implements CapabilityFactory
{

    private final P2MetadataGenerator service;

    @Inject
    P2MetadataGeneratorCapabilityFactory( final P2MetadataGenerator service )
    {
        this.service = service;
    }

    @Override
    public Capability create( final String id )
    {
        final P2MetadataGeneratorCapability capability = new P2MetadataGeneratorCapability( id, service );
        return capability;
    }

}
