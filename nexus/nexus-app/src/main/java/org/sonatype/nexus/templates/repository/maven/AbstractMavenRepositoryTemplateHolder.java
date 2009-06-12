package org.sonatype.nexus.templates.repository.maven;

import org.sonatype.nexus.Nexus;
import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.templates.repository.AbstractRepositoryTemplateHolder;

public abstract class AbstractMavenRepositoryTemplateHolder
    extends AbstractRepositoryTemplateHolder
{
    private RepositoryPolicy repositoryPolicy;

    public AbstractMavenRepositoryTemplateHolder( Nexus nexus, ContentClass contentClass, Class<?> mainFacet,
                                                  RepositoryPolicy repositoryPolicy )
    {
        super( nexus, contentClass, mainFacet );

        this.repositoryPolicy = repositoryPolicy;
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        return repositoryPolicy;
    }

    public void setRepositoryPolicy( RepositoryPolicy repositoryPolicy )
    {
        this.repositoryPolicy = repositoryPolicy;
    }
}
