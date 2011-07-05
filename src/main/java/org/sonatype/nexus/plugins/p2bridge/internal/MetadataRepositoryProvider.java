package org.sonatype.nexus.plugins.p2bridge.internal;

import javax.inject.Inject;
import javax.inject.Provider;

import org.sonatype.p2.bridge.MetadataRepository;

//@Named
//@Singleton
public class MetadataRepositoryProvider
    implements Provider<MetadataRepository>
{

    private final P2Runtime p2Runtime;

    private MetadataRepository service;

    @Inject
    public MetadataRepositoryProvider( final P2Runtime p2Runtime )
    {
        this.p2Runtime = p2Runtime;
    }

    @Override
    public MetadataRepository get()
    {
        if ( service == null )
        {
            service = p2Runtime.get().getService( MetadataRepository.class );
        }
        return service;
    }

}
