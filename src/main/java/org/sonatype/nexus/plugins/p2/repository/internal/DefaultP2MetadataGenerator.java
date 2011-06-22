package org.sonatype.nexus.plugins.p2.repository.internal;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGenerator;
import org.sonatype.nexus.plugins.p2.repository.P2MetadataGeneratorConfiguration;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.registry.RepositoryRegistry;

@Named
@Singleton
public class DefaultP2MetadataGenerator
    implements P2MetadataGenerator
{

    @Inject
    private Logger logger;

    private final Map<String, P2MetadataGeneratorConfiguration> configurations;

    private final RepositoryRegistry repositories;

    @Inject
    public DefaultP2MetadataGenerator( final RepositoryRegistry repositories )
    {
        this.repositories = repositories;
        configurations = new HashMap<String, P2MetadataGeneratorConfiguration>();
    }

    @Override
    public P2MetadataGeneratorConfiguration getConfiguration( final String repositoryId )
    {
        return configurations.get( repositoryId );
    }

    @Override
    public void addConfiguration( final P2MetadataGeneratorConfiguration configuration )
    {
        configurations.put( configuration.repositoryId(), configuration );
    }

    @Override
    public void removeConfiguration( final P2MetadataGeneratorConfiguration configuration )
    {
        configurations.remove( configuration.repositoryId() );
    }

    @Override
    public void generateP2Metadata( final StorageItem item )
    {
        final P2MetadataGeneratorConfiguration configuration = getConfiguration( item.getRepositoryId() );
        if ( configuration == null )
        {
            return;
        }
        logger.debug( "Generate P2 metadata for [{}:{}]", item.getRepositoryId(), item.getPath() );
    }

    @Override
    public void removeP2Metadata( final StorageItem item )
    {
        final P2MetadataGeneratorConfiguration configuration = getConfiguration( item.getRepositoryId() );
        if ( configuration == null )
        {
            return;
        }
        logger.debug( "Removing P2 metadata for [{}:{}]", item.getRepositoryId(), item.getPath() );
    }

}
