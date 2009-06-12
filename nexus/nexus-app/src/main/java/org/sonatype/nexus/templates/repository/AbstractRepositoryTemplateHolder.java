package org.sonatype.nexus.templates.repository;

import java.io.IOException;

import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.CoreConfiguration;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.TemplateHolder;

public abstract class AbstractRepositoryTemplateHolder
    implements TemplateHolder<Repository>
{
    private final Nexus nexus;

    private final CoreConfiguration configuration;

    private final ContentClass contentClass;

    private final Class<?> mainFacet;

    public AbstractRepositoryTemplateHolder( Nexus nexus, ContentClass contentClass, Class<?> mainFacet )
    {
        this.nexus = nexus;

        this.contentClass = contentClass;

        this.mainFacet = mainFacet;

        this.configuration = initConfiguration();
    }

    public Class<Repository> getImplementationClass()
    {
        return Repository.class;
    }

    public CoreConfiguration getConfiguration()
    {
        return configuration;
    }

    public ContentClass getContentClass()
    {
        return contentClass;
    }

    public Class<?> getMainFacet()
    {
        return mainFacet;
    }

    public Repository create()
        throws ConfigurationException, IOException
    {
        getConfiguration().applyChanges();

        return nexus.createRepository( (CRepository) getConfiguration().getConfiguration( false ) );
    }

    // ==

    protected abstract CoreConfiguration initConfiguration();
}
