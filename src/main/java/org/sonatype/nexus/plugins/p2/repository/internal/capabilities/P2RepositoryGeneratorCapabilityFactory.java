package org.sonatype.nexus.plugins.p2.repository.internal.capabilities;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryGenerator;

@Named( P2RepositoryGeneratorCapability.ID )
@Singleton
public class P2RepositoryGeneratorCapabilityFactory
    implements CapabilityFactory
{

    private final P2RepositoryGenerator service;

    @Inject
    P2RepositoryGeneratorCapabilityFactory( final P2RepositoryGenerator service )
    {
        this.service = service;
    }

    @Override
    public Capability create( final String id )
    {
        final P2RepositoryGeneratorCapability capability = new P2RepositoryGeneratorCapability( id, service );
        return capability;
    }

}
