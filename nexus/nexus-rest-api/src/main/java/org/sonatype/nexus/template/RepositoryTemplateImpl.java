package org.sonatype.nexus.template;

import org.sonatype.nexus.rest.model.RepositoryBaseResource;

public class RepositoryTemplateImpl
    implements RepositoryTemplate
{

    private String id;

    private RepositoryBaseResource content;

    public RepositoryTemplateImpl( String id, RepositoryBaseResource content )
    {
        super();
        this.id = id;
        this.content = content;
    }

    public RepositoryBaseResource getContent()
    {
        return content;
    }

    public String getId()
    {
        return id;
    }

}
