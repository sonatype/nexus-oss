package org.sonatype.nexus.plugins.p2.repository.internal.capabilities;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.capabilities.api.Capability;
import org.sonatype.nexus.plugins.capabilities.api.CapabilityFactory;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryAggregator;

@Named( P2RepositoryAggregatorCapability.ID )
@Singleton
public class P2RepositoryAggregatorCapabilityFactory
    implements CapabilityFactory
{

    private final P2RepositoryAggregator service;

    @Inject
    P2RepositoryAggregatorCapabilityFactory( final P2RepositoryAggregator service )
    {
        this.service = service;
    }

    @Override
    public Capability create( final String id )
    {
        final P2RepositoryAggregatorCapability capability = new P2RepositoryAggregatorCapability( id, service );
        return capability;
    }

}
