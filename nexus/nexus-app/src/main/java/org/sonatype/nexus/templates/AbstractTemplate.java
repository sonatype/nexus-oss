package org.sonatype.nexus.templates;

public abstract class AbstractTemplate
    implements Template
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
}
