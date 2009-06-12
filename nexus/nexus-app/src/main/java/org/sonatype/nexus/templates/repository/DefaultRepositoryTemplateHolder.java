package org.sonatype.nexus.templates.repository;

import java.io.IOException;

import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.TemplateHolder;

public class DefaultRepositoryTemplateHolder
    implements TemplateHolder<Repository>
{
    private final Nexus nexus;

    private CRepository configuration;

    public DefaultRepositoryTemplateHolder( Nexus nexus, CRepository configuration )
    {
        this.nexus = nexus;

        this.configuration = configuration;
    }

    public Class<Repository> getImplementationClass()
    {
        return Repository.class;
    }

    public CRepository getConfiguration()
    {
        return configuration;
    }

    public Repository create()
        throws ConfigurationException, IOException
    {
        return nexus.createRepository( getConfiguration() );

        // do somethning more?
    }

}
