package org.sonatype.nexus.templates.repository;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.configuration.model.CRepository;
import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.registry.RepositoryTypeRegistry;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.AbstractTemplateProvider;
import org.sonatype.nexus.templates.TemplateSet;

/**
 * An abstract class for template providers that provides templates for Repositories.
 * 
 * @author cstamas
 */
public abstract class AbstractRepositoryTemplateProvider
    extends AbstractTemplateProvider<RepositoryTemplate>
{
    @Requirement
    private RepositoryTypeRegistry repositoryTypeRegistry;

    @Requirement
    private Nexus nexus;

    protected Repository createRepository( CRepository repository )
        throws ConfigurationException, IOException
    {
        return this.nexus.getNexusConfiguration().createRepository( repository );
    }

    public Class<RepositoryTemplate> getTemplateClass()
    {
        return RepositoryTemplate.class;
    }

    public TemplateSet getTemplates( Object filter )
    {
        return getTemplates().getTemplates( filter );
    }

    public TemplateSet getTemplates( Object... filters )
    {
        return getTemplates().getTemplates( filters );
    }

    public ManuallyConfiguredRepositoryTemplate createManuallyTemplate( CRepositoryCoreConfiguration configuration )
    {
        ContentClass contentClass =
            repositoryTypeRegistry.getRepositoryContentClass(
                configuration.getConfiguration( false ).getProviderRole(), configuration.getConfiguration( false )
                    .getProviderHint() );

        return new ManuallyConfiguredRepositoryTemplate( this, "manual", "Manually created template", contentClass,
            null, configuration );
    }
}
