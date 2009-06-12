package org.sonatype.nexus.templates.repository;

import java.util.List;

import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.templates.NoSuchTemplateIdException;
import org.sonatype.nexus.templates.Template;

public class DefaultRepositoryTemplateProvider
    implements RepositoryTemplateProvider
{
    public Class<Repository> getImplementationClass()
    {
        return Repository.class;
    }

    public List<Template<Repository>> getTemplates()
    {
        // return a list (static and static inited maybe) of the 5 "known" repo
        return null;
    }

    public Template<Repository> getTemplateById( String id )
        throws NoSuchTemplateIdException
    {
        // TODO Auto-generated method stub
        return null;
    }
}
