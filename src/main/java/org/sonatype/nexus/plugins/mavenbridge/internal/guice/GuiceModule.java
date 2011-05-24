package org.sonatype.nexus.plugins.mavenbridge.internal.guice;

import javax.inject.Named;
import javax.inject.Singleton;

import org.sonatype.sisu.maven.bridge.internal.ModelResolverFactory;

import com.google.inject.AbstractModule;

@Named
@Singleton
public class GuiceModule
    extends AbstractModule
{
    @Override
    protected void configure()
    {
        bind( ModelResolverFactory.class ).to( NexusModelResolverFactory.class );
    }
}
