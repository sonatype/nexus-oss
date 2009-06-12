package org.sonatype.nexus.templates.repository;

import org.sonatype.nexus.proxy.maven.RepositoryPolicy;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.AbstractTemplate;
import org.sonatype.nexus.templates.TemplateHolder;

public class DefaultRepositoryTemplate
    extends AbstractTemplate<Repository>
    implements RepositoryTemplate
{
    public DefaultRepositoryTemplate( String id, String description )
    {
        super( id, description );
    }

    public TemplateHolder<Repository> getTemplateHolder()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public ContentClass getContentClass()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Class<?> getMainFacet()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public RepositoryPolicy getRepositoryPolicy()
    {
        // TODO Auto-generated method stub
        return null;
    }
}
