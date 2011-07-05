package org.sonatype.nexus.plugins.p2bridge.internal.guice;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.nexus.plugins.p2bridge.internal.ArtifactRepositoryProvider;
import org.sonatype.nexus.plugins.p2bridge.internal.MetadataRepositoryProvider;
import org.sonatype.nexus.plugins.p2bridge.internal.PublisherProvider;
import org.sonatype.p2.bridge.ArtifactRepository;
import org.sonatype.p2.bridge.MetadataRepository;
import org.sonatype.p2.bridge.Publisher;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

@Named
@Singleton
public class GuiceModule
    extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind( ArtifactRepository.class ).toProvider( ArtifactRepositoryProvider.class ).in( Scopes.SINGLETON );
        bind( MetadataRepository.class ).toProvider( MetadataRepositoryProvider.class ).in( Scopes.SINGLETON );
        bind( Publisher.class ).toProvider( PublisherProvider.class ).in( Scopes.SINGLETON );
    }
}