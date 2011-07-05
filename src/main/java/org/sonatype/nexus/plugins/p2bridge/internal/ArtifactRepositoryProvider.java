package org.sonatype.nexus.plugins.p2bridge.internal;

import javax.inject.Inject;
import javax.inject.Provider;

import org.sonatype.p2.bridge.ArtifactRepository;

//@Named
//@Singleton
public class ArtifactRepositoryProvider
    implements Provider<ArtifactRepository>
{

    private final P2Runtime p2Runtime;

    private ArtifactRepository service;

    @Inject
    public ArtifactRepositoryProvider( final P2Runtime p2Runtime )
    {
        this.p2Runtime = p2Runtime;
    }

    @Override
    public ArtifactRepository get()
    {
        if ( service == null )
        {
            service = p2Runtime.get().getService( ArtifactRepository.class );
        }
        return service;
    }

}
