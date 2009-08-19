package org.sonatype.nexus.templates;

public abstract class AbstractTemplate
    implements Template
{
    private final TemplateProvider provider;

    private final String id;

    private final String description;

    public AbstractTemplate( TemplateProvider provider, String id, String description )
    {
        this.provider = provider;

        this.id = id;

        this.description = description;
    }

    public TemplateProvider getTemplateProvider()
    {
        return provider;
    }

    public String getId()
    {
        return id;
    }

    public String getDescription()
    {
        return description;
    }

    public boolean targetFits( Object clazz )
    {
        return targetIsClassAndFitsClass( clazz, getClass() );
    }

    // ==

    protected boolean targetIsClassAndFitsClass( Object filter, Class<?> clazz )
    {
        if ( filter instanceof Class<?> )
        {
            return ( (Class<?>) filter ).isAssignableFrom( getClass() );
        }
        else
        {
            return false;
        }
    }
}
