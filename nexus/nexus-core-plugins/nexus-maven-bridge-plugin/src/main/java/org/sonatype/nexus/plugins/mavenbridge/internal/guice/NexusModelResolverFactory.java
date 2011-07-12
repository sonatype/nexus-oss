package org.sonatype.nexus.plugins.mavenbridge.internal.guice;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.maven.model.resolution.ModelResolver;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.impl.RemoteRepositoryManager;
import org.sonatype.sisu.maven.bridge.internal.ModelResolverFactory;
import org.sonatype.sisu.maven.bridge.resolvers.EmptyRemoteModelResolver;

@Singleton
public class NexusModelResolverFactory
    implements ModelResolverFactory
{
    private final RepositorySystem repositorySystem;

    private final RemoteRepositoryManager remoteRepositoryManager;

    @Inject
    public NexusModelResolverFactory( final RepositorySystem repositorySystem,
                                      final RemoteRepositoryManager remoteRepositoryManager )
    {
        this.repositorySystem = repositorySystem;
        this.remoteRepositoryManager = remoteRepositoryManager;
    }

    @Override
    public ModelResolver getModelResolver( RepositorySystemSession session )
    {
        return new EmptyRemoteModelResolver( repositorySystem, session, remoteRepositoryManager );
    }
}
