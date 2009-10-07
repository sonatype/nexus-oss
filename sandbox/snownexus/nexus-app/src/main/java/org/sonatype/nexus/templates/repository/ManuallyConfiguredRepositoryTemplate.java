package org.sonatype.nexus.templates.repository;

import org.sonatype.nexus.configuration.model.CRepositoryCoreConfiguration;
import org.sonatype.nexus.proxy.registry.ContentClass;

public class ManuallyConfiguredRepositoryTemplate
    extends AbstractRepositoryTemplate
{
    private final CRepositoryCoreConfiguration repoConfig;

    public ManuallyConfiguredRepositoryTemplate( AbstractRepositoryTemplateProvider provider, String id,
                                                 String description, ContentClass contentClass, Class<?> mainFacet,
                                                 CRepositoryCoreConfiguration repoConfig )
    {
        super( provider, id, description, contentClass, mainFacet );

        this.repoConfig = repoConfig;
    }

    @Override
    protected CRepositoryCoreConfiguration initCoreConfiguration()
    {
        return repoConfig;
    }
}
