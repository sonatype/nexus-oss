package org.sonatype.nexus.templates.repository;

import java.io.IOException;

import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.ConfigurableRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.AbstractConfigurableTemplate;

public abstract class AbstractRepositoryTemplate
    extends AbstractConfigurableTemplate
    implements RepositoryTemplate
{
    private final DefaultRepositoryTemplateProvider provider;

    private final ContentClass contentClass;

    private final Class<?> mainFacet;

    private ConfigurableRepository configurableRepository;

    public AbstractRepositoryTemplate( DefaultRepositoryTemplateProvider provider, String id, String description,
                                       ContentClass contentClass, Class<?> mainFacet )
    {
        super( provider, id, description );

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

    @Override
    public boolean targetFits( Object clazz )
    {
        return super.targetFits( clazz ) || targetIsClassAndFitsClass( clazz, getMainFacet() )
            || ( targetIsClassAndFitsClass( clazz, getContentClass().getClass() ) || getContentClass().equals( clazz ) );
    }

    @Override
    public DefaultRepositoryTemplateProvider getTemplateProvider()
    {
        return provider;
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
        if ( configurableRepository == null )
        {
            configurableRepository = new ConfigurableRepository();

            try
            {
                configurableRepository.configure( getCoreConfiguration() );
            }
            catch ( ConfigurationException e )
            {
                // will not happen, since ConfigurableRepository will not validate!
                // TODO: get rid of this exception from here
            }
        }
        return configurableRepository;
    }

    public Repository create()
        throws ConfigurationException, IOException
    {
        getCoreConfiguration().validateChanges();

        // to merge in user changes to CoreConfiguration
        getCoreConfiguration().commitChanges();

        // create a repository
        Repository repository =
            getTemplateProvider().getNexus().getNexusConfiguration().createRepository(
                ( getCoreConfiguration() ).getConfiguration( false ) );

        // reset the template
        setCoreConfiguration( null );

        // return the result
        return repository;
    }

    public String getRepositoryProviderRole()
    {
        return getCoreConfiguration().getConfiguration( false ).getProviderRole();
    }

    public String getRepositoryProviderHint()
    {
        return getCoreConfiguration().getConfiguration( false ).getProviderHint();
    }

    public CRepositoryCoreConfiguration getCoreConfiguration()
    {
        // we may do this, since we predefined the initCoreConfiguration(), see below
        return (CRepositoryCoreConfiguration) super.getCoreConfiguration();
    }

    // ==

    @Override
    protected abstract CRepositoryCoreConfiguration initCoreConfiguration();
}
