package org.sonatype.nexus.templates.repository;

import java.io.IOException;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ConfigurableRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.AbstractConfigurableTemplate;

public class RepositoryTemplate
    extends AbstractConfigurableTemplate<Repository>
{
    private final DefaultRepositoryTemplateProvider provider;

    private final ContentClass contentClass;

    private final Class<?> mainFacet;

    private final ConfigurableRepository configurableRepository;

    public RepositoryTemplate( DefaultRepositoryTemplateProvider provider, String id, String description,
                               CRepository config, ContentClass contentClass )
        throws ConfigurationException
    {
        this( provider, id, description, config, contentClass, null );
    }

    public RepositoryTemplate( DefaultRepositoryTemplateProvider provider, String id, String description,
                               CRepository config, ContentClass contentClass, Class<?> mainFacet )
        throws ConfigurationException
    {
        super( id, description, new CRepositoryCoreConfiguration( config ) );

        this.provider = provider;

        this.contentClass = contentClass;

        if ( mainFacet != null )
        {
            this.mainFacet = mainFacet;
        }
        else
        {
            this.mainFacet = Repository.class;
        }

        this.configurableRepository = new ConfigurableRepository();

        this.configurableRepository.configure( getCoreConfiguration() );
    }

    public ContentClass getContentClass()
    {
        return contentClass;
    }

    public Class<?> getMainFacet()
    {
        return mainFacet;
    }

    public ConfigurableRepository getConfigurableRepository()
    {
        return configurableRepository;
    }
    
    public Class<Repository> getTargetClass()
    {
        return provider.getTargetClass();
    }

    public Repository create()
        throws ConfigurationException, IOException
    {
        getCoreConfiguration().applyChanges();

        return provider.getNexus().createRepository( (CRepository) getCoreConfiguration().getConfiguration( false ) );
    }
}
