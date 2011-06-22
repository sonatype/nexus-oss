package org.sonatype.nexus.plugins.p2.repository.internal;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryGenerator;
import org.sonatype.nexus.plugins.p2.repository.P2RepositoryGeneratorConfiguration;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

@Named
@Singleton
public class DefaultP2RepositoryGenerator
    implements P2RepositoryGenerator
{

    @Inject
    private Logger logger;

    private final Map<String, P2RepositoryGeneratorConfiguration> configurations;

    private final RepositoryRegistry repositories;

    @Inject
    public DefaultP2RepositoryGenerator( final RepositoryRegistry repositories )
    {
        this.repositories = repositories;
        configurations = new HashMap<String, P2RepositoryGeneratorConfiguration>();
    }

    @Override
    public P2RepositoryGeneratorConfiguration getConfiguration( final String repositoryId )
    {
        return configurations.get( repositoryId );
    }

    @Override
    public void addConfiguration( final P2RepositoryGeneratorConfiguration configuration )
    {
        configurations.put( configuration.repositoryId(), configuration );
    }

    @Override
    public void removeConfiguration( final P2RepositoryGeneratorConfiguration configuration )
    {
        configurations.remove( configuration.repositoryId() );
    }

}
