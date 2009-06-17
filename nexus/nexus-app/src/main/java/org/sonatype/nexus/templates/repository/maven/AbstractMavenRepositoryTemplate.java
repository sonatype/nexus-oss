package org.sonatype.nexus.templates.repository.maven;

import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplate;
import org.sonatype.nexus.templates.repository.DefaultRepositoryTemplateProvider;

public abstract class AbstractMavenRepositoryTemplate
    extends AbstractRepositoryTemplate
{
    private RepositoryPolicy repositoryPolicy;

    public AbstractMavenRepositoryTemplate( DefaultRepositoryTemplateProvider provider, String id, String description,
                                            ContentClass contentClass, Class<?> mainFacet,
                                            RepositoryPolicy repositoryPolicy )
    {
        super( provider, id, description, contentClass, mainFacet );
        
        setRepositoryPolicy( repositoryPolicy );
    }

    protected RepositoryPolicy getRepositoryPolicy()
    {
        return repositoryPolicy;
    }

    protected void setRepositoryPolicy( RepositoryPolicy repositoryPolicy )
    {
        this.repositoryPolicy = repositoryPolicy;
    }
}
