package org.sonatype.nexus.templates.repository;

import java.io.IOException;

import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ConfigurableRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.AbstractConfigurableTemplate;

public abstract class AbstractRepositoryTemplate
    extends AbstractConfigurableTemplate<Repository>
{
    private final DefaultRepositoryTemplateProvider provider;

    private final ContentClass contentClass;

    private final Class<?> mainFacet;

    public AbstractRepositoryTemplate( DefaultRepositoryTemplateProvider provider, String id, String description,
                               ContentClass contentClass, Class<?> mainFacet )
    {
        super( id, description );

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
        ConfigurableRepository configurableRepository = new ConfigurableRepository();

        try
        {
            configurableRepository.configure( getCoreConfiguration() );
        }
        catch ( ConfigurationException e )
        {
            // will not happen, since ConfigurableRepository will not validate!
            // TODO: get rid of this exception from here
        }

        return configurableRepository;
    }

    public Class<Repository> getTargetClass()
    {
        return provider.getTargetClass();
    }

    public Repository create()
        throws ConfigurationException, IOException
    {
        // to merge in user changes to CoreConfiguration
        getCoreConfiguration().applyChanges();

        // create a repository
        Repository result =
            provider.getNexus().createRepository( (CRepository) getCoreConfiguration().getConfiguration( false ) );

        // reset the template
        setCoreConfiguration( null );

        // return the result
        return result;
    }
}
