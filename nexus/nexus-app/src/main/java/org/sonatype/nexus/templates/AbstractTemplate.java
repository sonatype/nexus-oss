package org.sonatype.nexus.templates;

import java.io.IOException;

import org.sonatype.nexus.configuration.ConfigurationException;

public abstract class AbstractTemplate<I>
    implements Template<I>
{
    private final String id;

    private final String description;

    public AbstractTemplate( String id, String description )
    {
        this.id = id;

        this.description = description;
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public Class<I> getImplementationClass()
    {
        return getTemplateHolder().getImplementationClass();
    }

    public I create()
        throws ConfigurationException, IOException
    {
        return getTemplateHolder().create();
    }
}
