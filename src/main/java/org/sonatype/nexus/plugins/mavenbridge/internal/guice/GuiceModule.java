package org.sonatype.nexus.plugins.mavenbridge.internal.guice;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.resolution.ModelResolver;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.sisu.maven.bridge.internal.RepositorySystemSessionProvider;
import org.sonatype.sisu.maven.bridge.resolvers.EmptyRemoteModelResolver;

import com.google.inject.AbstractModule;

@Named
@Singleton
public class GuiceModule
    extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind( ModelResolver.class ).to( EmptyRemoteModelResolver.class );
        bind( RepositorySystemSession.class ).toProvider( RepositorySystemSessionProvider.class );
    }
}
