package org.sonatype.nexus.templates.repository;

import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.AbstractTemplate;

public abstract class RepositoryTemplate
    extends AbstractTemplate<Repository>
{
    private final AbstractRepositoryTemplateHolder templateHolder;

    public RepositoryTemplate( String id, String description, AbstractRepositoryTemplateHolder templateHolder )
    {
        super( id, description );

        this.templateHolder = templateHolder;
    }

    public AbstractRepositoryTemplateHolder getTemplateHolder()
    {
        return templateHolder;
    }

    public ContentClass getContentClass()
    {
        return getTemplateHolder().getContentClass();
    }

    public Class<?> getMainFacet()
    {
        return getTemplateHolder().getMainFacet();
    }
}
