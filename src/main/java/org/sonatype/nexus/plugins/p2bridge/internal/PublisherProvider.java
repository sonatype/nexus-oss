package org.sonatype.nexus.plugins.p2bridge.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.sonatype.p2.bridge.Publisher;

@Named
@Singleton
public class PublisherProvider
    implements Provider<Publisher>
{

    private final P2Runtime p2Runtime;

    private Publisher service;

    @Inject
    public PublisherProvider( final P2Runtime p2Runtime )
    {
        this.p2Runtime = p2Runtime;
    }

    @Override
    public Publisher get()
    {
        if ( service == null )
        {
            service = p2Runtime.get().getService( Publisher.class );
        }
        return service;
    }

}
